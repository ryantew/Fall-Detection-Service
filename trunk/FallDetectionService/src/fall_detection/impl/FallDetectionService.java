package fall_detection.impl;

import com.phidgets.InterfaceKitPhidget;
import com.phidgets.Phidget;
import com.phidgets.PhidgetException;
import com.phidgets.event.AttachEvent;
import com.phidgets.event.AttachListener;
import com.phidgets.event.SensorChangeEvent;
import com.phidgets.event.SensorChangeListener;

import fall_detection.FallDetection;

public class FallDetectionService implements FallDetection
{
	private static final int INTERFACE_KIT_SERIAL_NUM = 76397;
	
	private static final double SENSOR_CONVERSION_FACTOR = 25.71;
	
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

        ifk.open(INTERFACE_KIT_SERIAL_NUM);
        
        try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FallDetectionService() throws PhidgetException
	{
		InterfaceKitPhidget ifk = new InterfaceKitPhidget();
	}
	
	@Override
	public void start()
	{
		// TODO Auto-generated method stub
		
	}
}
