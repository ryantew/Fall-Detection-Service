package fall_detection.impl;

import java.io.IOException;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import edu.iastate.service.notification.Notification;
import edu.iastate.sh.services.speech.SpeechService;
import fall_detection.FallDetection;

public class FallDetectionService implements FallDetection
{
	private static final double FORCE_TO_CANCEL = 10;
	
	private static final int NUM_INTERFACE_KIT_PORTS = 8;
	
	private static final int INTERFACE_KIT_SERIAL_NUM_0 = 76397;
	
	private static final int SENSOR_GRID_WIDTH = 3;
	private static final int SENSOR_GRID_HEIGHT = 3;
	
	private static final double SENSOR_CONVERSION_FACTOR = 25.71;
	
	private static final int CANCEL_WAIT_TIME_SECONDS = 15;
	
	private static final String FALL_DETECTED = "A fall has been detected.";
	private static final String CANCEL_NOTIFICATION = "Press the cancel button if this is a false alarm.";
	private static final String CANCEL_RECEIVED = "Fall alarm canceled.";
	
	private double[][] sensorArray;
	private boolean canceled;
	
	private Notification notifier;
	private SpeechService speaker;
	
	//test method
	public static void main(String args[]) throws PhidgetException
	{
		InterfaceKitPhidget ifk = new InterfaceKitPhidget();
        ifk.addAttachListener(new AttachListener()
        {			
			@Override
			public void attached(AttachEvent e)
			{
				Phidget p = e.getSource();
				try
				{
					System.out.println(p.getDeviceClass());
					System.out.println(p.getDeviceID());
					System.out.println(p.getDeviceLabel());
					System.out.println(p.getDeviceName());
					System.out.println(p.getDeviceType());
					System.out.println(p.getDeviceVersion());
					System.out.println(p.getSerialNumber());
					System.out.println();
				}
				catch (PhidgetException e1)
				{
					e1.printStackTrace();
				}
			}
		});
        
        ifk.addSensorChangeListener(new SensorChangeListener()
        {
			@Override
			public void sensorChanged(SensorChangeEvent e)
			{
				Phidget p = e.getSource();
				try
				{
					System.out.println(e.getValue());
					System.out.println(p.getDeviceClass());
					System.out.println(p.getDeviceID());
					System.out.println(p.getDeviceLabel());
					System.out.println(p.getDeviceName());
					System.out.println(p.getDeviceType());
					System.out.println(p.getDeviceVersion());
					System.out.println(p.getSerialNumber());
					System.out.println();
				}
				catch (PhidgetException e1)
				{
					e1.printStackTrace();
				}
				
			}
		});

        ifk.open(INTERFACE_KIT_SERIAL_NUM_0);
        
        try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FallDetectionService(Notification notifier, SpeechService speaker) throws PhidgetException
	{
		this.notifier = notifier;
		this.speaker = speaker;
		
		this.sensorArray = new double[SENSOR_GRID_HEIGHT][SENSOR_GRID_WIDTH];
		
		InterfaceKitPhidget[] ifk = new InterfaceKitPhidget[1];
		
		ifk[0] = new InterfaceKitPhidget();
		ifk[0].addSensorChangeListener(new PressureSensorListener(0));
		ifk[0].addSensorChangeListener(new CancelButtonListener());
		ifk[0].open(INTERFACE_KIT_SERIAL_NUM_0);
	}
	
	@Override
	public void start()
	{
		// TODO Auto-generated method stub
		
	}
	
	private void detectFall()
	{
		//TODO fall detection
		
		//TODO if fall is detected sound alarm
	}
	
	private void soundAlarm()
	{
		canceled = false;

		try
		{
			speaker.speak(FallDetectionService.FALL_DETECTED);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		try
		{
			speaker.speak(FallDetectionService.CANCEL_NOTIFICATION);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		
		try
		{
			wait(FallDetectionService.CANCEL_WAIT_TIME_SECONDS * 1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		if(canceled)
		{
			try
			{
				speaker.speak(FallDetectionService.CANCEL_RECEIVED);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			//TODO notify the contact list
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
			int r = i / FallDetectionService.SENSOR_GRID_WIDTH;
			int c = i % FallDetectionService.SENSOR_GRID_WIDTH;
			
			FallDetectionService.this.sensorArray[r][c] = e.getValue() / SENSOR_CONVERSION_FACTOR;
			FallDetectionService.this.detectFall();
		}
	}
	
	private class CancelButtonListener implements SensorChangeListener
	{
		@Override
		public void sensorChanged(SensorChangeEvent e)
		{
			if(e.getValue() / SENSOR_CONVERSION_FACTOR >= FallDetectionService.FORCE_TO_CANCEL)
			{
				FallDetectionService.this.canceled = true;
				FallDetectionService.this.notify();
			}
		}
	}
}
