package fall_detection.impl;

import java.util.ArrayList;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import edu.iastate.service.notification.Notification;
import edu.iastate.service.notification.impl.NotificationImpl;
import edu.iastate.sh.services.speech.SpeechService;
import edu.iastate.sh.services.speech.impl.SpeechServiceImpl;
import fall_detection.ContactListService;
import fall_detection.FallDetectionService;

/**
 * 
 * The Fall Detection Service
 * @author Ryan Tew
 * @author John George
 *
 */
public class FallDetection implements FallDetectionService
{
	public static final String CONTACT_FILE = "resources/contacts.txt";
	
	/**
	 * Force to cancel in lbs
	 */
	private static final double FORCE_TO_CANCEL = 10;
	
	/**
	 * The number of ports on each interface kit
	 */
	private static final int NUM_INTERFACE_KIT_PORTS = 8;

	/**
	 * The serial number of each IFK in the order they will be connected
	 */
	private static final int INTERFACE_KIT_SERIAL_NUM_0 = 107381;
	private static final int INTERFACE_KIT_SERIAL_NUM_1 = 76397;
	
	/**
	 * The dimensions of the grid
	 */
	private static final int SENSOR_GRID_COLUMNS = 5;
	private static final int SENSOR_GRID_ROWS = 3;

	/**
	 * The conversion factor between the values from the sensors and their value in lbs.
	 * Value in lbs = sensor value / SENSOR_CONVERSION_FACTOR
	 */
	private static final double SENSOR_CONVERSION_FACTOR = 25.71;

	/**
	 * The time to wait for the cancel button to be pressed in seconds
	 */
	private static final int CANCEL_WAIT_TIME_SECONDS = 15;
	
	/**
	 * Constants for sensitivity of the sensor grid
	 */
	private static final int NUM_TILES_TO_DETECT = 4;
	private static final double DETECTION_WEIGHT_THRESHOLD = 0.5;
	private static final int DETECTION_RADIUS = 2;

	/**
	 * Strings messages for the speaker
	 */
	private static final String FALL_DETECTED = "A fall has been detected.";
	private static final String CANCEL_NOTIFICATION = "Press the cancel button if this is a false alarm.";
	private static final String CANCEL_RECEIVED = "Fall alarm canceled.";
	private static final String FALL_NOTIFICATION = "A fall has occurred at the smart home. Please check on the resident.";
	
	/**
	 * lock objects
	 */
	private Object cancelWait;
	private Object alarmLock;
	
	/**
	 * Interface kit references
	 */
	private InterfaceKitPhidget[] ifk;
	
	/**
	 * The array representing the floor sensors
	 */
	private double[][] sensorArray;
	
	/**
	 * code flow control booleans for the alarm (needed for multiple threads)
	 */
	private boolean canCanel;
	private boolean canceled;
	private boolean sounding;

	/**
	 * References to required services
	 */
	private Notification notifier;
	private SpeechService speaker;
	private ContactListService contactList;
	
	/**
	 * Test method
	 * @param args
	 * @throws PhidgetException
	 */
	public static void main(String args[]) throws PhidgetException
	{
		FallDetection service = new FallDetection(new NotificationImpl(), new SpeechServiceImpl(), new ContactList(CONTACT_FILE));
		service.start();

		//infinite run
		while(true)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets up the Interface Kits and push notifications
	 * @param notifier The email and text notifier
	 * @param speaker The smart home text to speech synthesizer
	 * @throws PhidgetException
	 */
	public FallDetection(Notification notifier, SpeechService speaker, ContactListService contacts) throws PhidgetException
	{
		//initialize required services
		this.notifier = notifier;
		this.speaker = speaker;
		this.contactList = contacts;
		
		//initialize lock objects
		this.cancelWait = new Object();
		this.alarmLock = new Object();
		
		//initialize flow control booleans
		this.canCanel = false;
		this.sounding = false;
		
		this.sensorArray = new double[SENSOR_GRID_ROWS][SENSOR_GRID_COLUMNS];
		
		ifk = new InterfaceKitPhidget[2];

		//initialize the interface kits and push notifications
		ifk[0] = new InterfaceKitPhidget();
		ifk[0].addSensorChangeListener(new PressureSensorListener(0));
		ifk[0].addSensorChangeListener(new CancelButtonListener());

		ifk[1] = new InterfaceKitPhidget();
		ifk[1].addSensorChangeListener(new PressureSensorListener(1));
	}

	@Override
	public double[][] getGrid()
	{
		return sensorArray;
	}

	@Override
	public boolean isFallDetected()
	{
		return sounding;
	}
	
	/**
	 * Start the fall detection service.
	 */
	public void start()
	{
		try
		{
			ifk[0].open(INTERFACE_KIT_SERIAL_NUM_0);
			ifk[1].open(INTERFACE_KIT_SERIAL_NUM_1);
		}
		catch(PhidgetException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Stop the fall detection service and release its resources.
	 */
	public void stop()
	{
		notifier = null;
		speaker = null;
		contactList = null;
		
		try
		{
			ifk[0].close();
			ifk[1].close();
		}
		catch (PhidgetException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Detect if there is a possible fall
	 * @param r The row of the tile that initiated the fall detection algorithm
	 * @param c The column of the tile that initiated the fall detection algorithm
	 */
	private void detectFall(int r, int c)
	{
		//do not detect a fall if the alarm is sounding
		if(sounding){
			return;
		}
		
		//check the tiles around the initiating tile
		int numTiles = 0;
		for(int i = r-DETECTION_RADIUS; i < r+DETECTION_RADIUS; i++){
			for(int j = c-DETECTION_RADIUS; j < c+DETECTION_RADIUS; j++){
				if(i >= 0 && j >= 0 && i < SENSOR_GRID_ROWS && j < SENSOR_GRID_COLUMNS && sensorArray[i][j] > DETECTION_WEIGHT_THRESHOLD){
						numTiles++;
				}
			}
		}

		//if we have enough tiles active, detect a fall
		if(numTiles >= NUM_TILES_TO_DETECT)
		{
			//sound the alarm in a new thread
			Thread t = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					soundAlarm();
				}
			});
			
			t.start();
		}
	}

	/**
	 * Sounds the alarm that a fall has been detected
	 */
	private void soundAlarm()
	{
		boolean soundAlarm = false;
		synchronized(alarmLock)
		{
			//if this is the first thread to sound the alarm
			if(!FallDetection.this.sounding)
			{
				//set the boolean to sound the alarm
				soundAlarm = true;
				
				canceled = false;
				sounding = true;
			}
		}

		//check that only one thread will pass
		if(!soundAlarm)
			return;
		
		System.out.println(FallDetection.FALL_DETECTED);
		try
		{
			speaker.speak(FallDetection.FALL_DETECTED);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println(FallDetection.CANCEL_NOTIFICATION);
		try
		{
			speaker.speak(FallDetection.CANCEL_NOTIFICATION);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		contactList.populateList();

		//wait to see if the cancel button is pressed
		synchronized(cancelWait)
		{
			canCanel = true;
			try
			{
				cancelWait.wait(FallDetection.CANCEL_WAIT_TIME_SECONDS * 1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		if(canceled)
		{
			//the alarm has been canceled
			System.out.println(FallDetection.CANCEL_RECEIVED);
			try
			{
				speaker.speak(FallDetection.CANCEL_RECEIVED);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			sounding = false;
		}
		else
		{
			//the alarm was not canceled, send notifications to the contacts
			ArrayList<Contact> contacts = contactList.getContacts();
			
			System.out.println(FallDetection.FALL_NOTIFICATION);
			for(Contact c : contacts)
			{
				if(!c.getEmail().equals("")){
					notifier.emailandtext(c.getEmail(), FallDetection.FALL_NOTIFICATION);
				}
				if(!c.getPhoneNum().equals("")){
					notifier.emailandtext(c.getPhoneNum(), FallDetection.FALL_NOTIFICATION);
				}
			}
			/*
			 * Should it cancel the alarm after sending the notifications or should it keep the alarm going,
			 * resending the notifications after some period of time until it's shut off?
			 */
		}
	}

	/**
	 * 
	 * Sensor listener for the floor sensors
	 * @author Ryan Tew
	 * @author John George
	 *
	 */
	private class PressureSensorListener implements SensorChangeListener
	{
		/**
		 * The starting index of the interface kit this sensor is attached to
		 */
		private int startingIndex;
		
		/**
		 * Calculates the startingIndex of the interface kit
		 * @param deviceIndex The index of the interface kit
		 */
		public PressureSensorListener(int deviceIndex)
		{
			this.startingIndex = deviceIndex * FallDetection.NUM_INTERFACE_KIT_PORTS - 1;
		}

		@Override
		public void sensorChanged(SensorChangeEvent e)
		{
			int i = e.getIndex() + startingIndex;
			if(i < 0 || i >= FallDetection.SENSOR_GRID_COLUMNS * FallDetection.SENSOR_GRID_ROWS)
				return;
			
			int r = i / FallDetection.SENSOR_GRID_COLUMNS;
			int c = i % FallDetection.SENSOR_GRID_COLUMNS;
			
			FallDetection.this.sensorArray[r][c] = e.getValue() / SENSOR_CONVERSION_FACTOR;
			FallDetection.this.detectFall(r, c);
		}
	}
	
	/**
	 * 
	 * Sensor listener for the cancel button
	 * @author Ryan Tew
	 * @author John George
	 *
	 */
	private class CancelButtonListener implements SensorChangeListener
	{
		@Override
		public void sensorChanged(SensorChangeEvent e)
		{
			if(e.getIndex() != 0)
				return;
			
			//check to make sure the alarm is waiting for a cancel
			if(FallDetection.this.canCanel && e.getValue() / SENSOR_CONVERSION_FACTOR >= FallDetection.FORCE_TO_CANCEL)
			{
				FallDetection.this.canceled = true;
				FallDetection.this.sounding = false;
				
				synchronized(FallDetection.this.cancelWait)
				{
					//notify the alarm that it has been canceled
					FallDetection.this.canCanel = false;
					FallDetection.this.cancelWait.notify();
				}
			}
		}
	}
}
