package fall_detection.impl;

import java.io.IOException;
import java.util.ArrayList;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import edu.iastate.service.notification.Notification;
import edu.iastate.service.notification.impl.NotificationImpl;
import edu.iastate.sh.services.speech.SpeechService;
import edu.iastate.sh.services.speech.impl.SpeechServiceImpl;
import fall_detection.IFallDetection;

public class FallDetectionService implements IFallDetection
{
	public static final String CONTACT_FILE = "resources/contacts.txt";
	
	private static final double FORCE_TO_CANCEL = 10;
	
	private static final int NUM_INTERFACE_KIT_PORTS = 8;
	
	private static final int INTERFACE_KIT_SERIAL_NUM_0 = 107381;
	private static final int INTERFACE_KIT_SERIAL_NUM_1 = 76397;
	
	private static final int SENSOR_GRID_WIDTH = 5;
	private static final int SENSOR_GRID_HEIGHT = 3;
	
	private static final double SENSOR_CONVERSION_FACTOR = 25.71;
	
	private static final int CANCEL_WAIT_TIME_SECONDS = 15;
	
	//This will need to be adjusted
	private static final int NUM_TILES_TO_DETECT = 4;
	private static final double DETECTION_WEIGHT_THRESHOLD = 0.5;
	private static final int DETECTION_RADIUS = 3;
	
	private static final String FALL_DETECTED = "A fall has been detected.";
	private static final String CANCEL_NOTIFICATION = "Press the cancel button if this is a false alarm.";
	private static final String CANCEL_RECEIVED = "Fall alarm canceled.";
	private static final String FALL_NOTIFICATION = "A fall has occurred at the smart home. Please check on the resident.";
	
	
	private Object cancelWait;
	private Object alarmLock;
	
	private InterfaceKitPhidget[] ifk;
	private double[][] sensorArray;
	private boolean canceled;
	private boolean sounding;
	
	private Notification notifier;
	private SpeechService speaker;
	private ContactList contactList;
	
	//test method
	public static void main(String args[]) throws PhidgetException
	{
		FallDetectionService service = new FallDetectionService(new NotificationImpl(), new SpeechServiceImpl());
		service.start();
		
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
	
	public FallDetectionService(Notification notifier, SpeechService speaker) throws PhidgetException
	{
		this.notifier = notifier;
		this.speaker = speaker;
		
		this.cancelWait = new Object();
		this.alarmLock = new Object();
		
		this.sounding = false;
		this.contactList = new ContactList(FallDetectionService.CONTACT_FILE);
		this.sensorArray = new double[SENSOR_GRID_HEIGHT][SENSOR_GRID_WIDTH];
		
		ifk = new InterfaceKitPhidget[2];
		
		ifk[0] = new InterfaceKitPhidget();
		ifk[0].addSensorChangeListener(new PressureSensorListener(0));
		ifk[0].addSensorChangeListener(new CancelButtonListener());

		ifk[1] = new InterfaceKitPhidget();
		ifk[1].addSensorChangeListener(new PressureSensorListener(1));
	}
	
	@Override
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
	 * This algorithm is slightly crappy/very simplistic, but it's a start.
	 * @param r
	 * @param c
	 */
	private void detectFall(int r, int c)
	{
		if(sounding){
			return;
		}
		int numTiles = 0;
		for(int i = r-DETECTION_RADIUS; i < r+DETECTION_RADIUS; i++){
			for(int j = c-DETECTION_RADIUS; j < c+DETECTION_RADIUS; j++){
				if(i >= 0 && j >= 0 && i < SENSOR_GRID_HEIGHT && j < SENSOR_GRID_WIDTH && sensorArray[i][j] > DETECTION_WEIGHT_THRESHOLD){
						numTiles++;
				}
			}
		}

		System.out.println(numTiles);
		if(numTiles >= NUM_TILES_TO_DETECT)
		{
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
	
	private void soundAlarm()
	{
		boolean soundAlarm = false;
		synchronized(alarmLock)
		{
			if(!FallDetectionService.this.sounding)
			{
				soundAlarm = true;
				
				canceled = false;
				sounding = true;
			}
		}
		
		if(!soundAlarm)
			return;
		
		System.out.println(FallDetectionService.FALL_DETECTED);
		try
		{
			speaker.speak(FallDetectionService.FALL_DETECTED);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		System.out.println(FallDetectionService.CANCEL_NOTIFICATION);
		try
		{
			speaker.speak(FallDetectionService.CANCEL_NOTIFICATION);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		contactList.populateList();
		
		synchronized(cancelWait)
		{
			try
			{
				cancelWait.wait(FallDetectionService.CANCEL_WAIT_TIME_SECONDS * 1000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		if(canceled)
		{
			System.out.println(FallDetectionService.CANCEL_RECEIVED);
			try
			{
				speaker.speak(FallDetectionService.CANCEL_RECEIVED);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			sounding = false;
		}
		else
		{
			ArrayList<Contact> contacts = contactList.getContacts();
			
			System.out.println(FallDetectionService.FALL_NOTIFICATION);
			for(Contact c : contacts)
			{
				if(c.getEmail() != ""){
					notifier.emailandtext(c.getEmail(), FallDetectionService.FALL_NOTIFICATION);
				}
				if(c.getPhoneNum() != ""){
					notifier.emailandtext(c.getPhoneNum(), FallDetectionService.FALL_NOTIFICATION);
				}
			}
			/*
			 * Should it cancel the alarm after sending the notifications or should it keep the alarm going,
			 * resending the notifications after some period of time until it's shut off?
			 */
		}
	}

	private class PressureSensorListener implements SensorChangeListener
	{
		private int startingIndex;
		
		public PressureSensorListener(int deviceIndex)
		{
			this.startingIndex = deviceIndex * FallDetectionService.NUM_INTERFACE_KIT_PORTS - 1;
		}

		@Override
		public void sensorChanged(SensorChangeEvent e)
		{
			int i = e.getIndex() + startingIndex;
			if(i < 0 || i >= FallDetectionService.SENSOR_GRID_WIDTH * FallDetectionService.SENSOR_GRID_HEIGHT)
				return;
			
			int r = i / FallDetectionService.SENSOR_GRID_WIDTH;
			int c = i % FallDetectionService.SENSOR_GRID_WIDTH;
			
			FallDetectionService.this.sensorArray[r][c] = e.getValue() / SENSOR_CONVERSION_FACTOR;
			FallDetectionService.this.detectFall(r, c);
		}
	}
	
	private class CancelButtonListener implements SensorChangeListener
	{
		@Override
		public void sensorChanged(SensorChangeEvent e)
		{
			if(e.getIndex() != 0)
				return;
			
			if(FallDetectionService.this.sounding && e.getValue() / SENSOR_CONVERSION_FACTOR >= FallDetectionService.FORCE_TO_CANCEL)
			{
				FallDetectionService.this.canceled = true;
				FallDetectionService.this.sounding = false;
				
				synchronized(FallDetectionService.this.cancelWait)
				{
					FallDetectionService.this.cancelWait.notify();
				}
			}
		}
	}
}
