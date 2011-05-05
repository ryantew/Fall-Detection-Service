package fall_detection.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import edu.iastate.service.notification.Notification;
import edu.iastate.sh.services.speech.SpeechService;
import fall_detection.IContactEditor;
import fall_detection.IFallDetection;

public class Activator implements BundleActivator
{
	private ContactEditor editor;
	private FallDetectionService fallDetection;
	
	@Override
	public void start(BundleContext bc) throws Exception
	{
		System.out.println(bc.getBundle().getHeaders().get(Constants.BUNDLE_NAME) + " starting...");

		Notification notifier = (Notification)bc.getService(bc.getServiceReference(Notification.class.getName()));
		SpeechService speaker = (SpeechService)bc.getService(bc.getServiceReference(SpeechService.class.getName()));
		
		editor = new ContactEditor();
		editor.start();
		bc.registerService(IContactEditor.class.getName(), editor, new Hashtable());
		System.out.println("Service registered: ContactEditor");
		
		fallDetection = new FallDetectionService(notifier, speaker);
		fallDetection.start();
		bc.registerService(IFallDetection.class.getName(), fallDetection, new Hashtable());
		System.out.println("Service registered: FallDetection");
	}

	@Override
	public void stop(BundleContext bc) throws Exception
	{
		System.out.println("Stoping datebundle...");

		fallDetection.stop();
		editor.stop();
		
		fallDetection = null;
		editor = null;
		
		bc.ungetService(bc.getServiceReference(Notification.class.getName()));
		bc.ungetService(bc.getServiceReference(SpeechService.class.getName()));
	}
}
