package fall_detection.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import contact_editor.impl.ContactEditor;
import edu.iastate.service.notification.Notification;
import edu.iastate.sh.services.speech.SpeechService;
import fall_detection.ContactListService;
import fall_detection.FallDetectionService;

/**
 * 
 * The Activator for the Fall Detection and Contact Editor services.
 * @author Ryan Tew
 * @author John George
 *
 */
public class Activator implements BundleActivator
{
	/**
	 * References to the provided services.
	 */
	private ContactEditor editor;
	private ContactList contactList;
	private FallDetection fallDetection;
	
	@Override
	public void start(BundleContext bc) throws Exception
	{
		System.out.println(bc.getBundle().getHeaders().get(Constants.BUNDLE_NAME) + " starting...");
		
		//start the contact list service
		contactList = new ContactList(FallDetection.CONTACT_FILE);
		bc.registerService(ContactListService.class.getName(), contactList, new Hashtable());
		System.out.println("Service registered: ContactListService");

		//get the required services
		Notification notifier = (Notification)bc.getService(bc.getServiceReference(Notification.class.getName()));
		SpeechService speaker = (SpeechService)bc.getService(bc.getServiceReference(SpeechService.class.getName()));
		ContactListService contactListService = (ContactListService)bc.getService(bc.getServiceReference(ContactListService.class.getName()));
	
		//start the fall detection service
		fallDetection = new FallDetection(notifier, speaker, contactListService);
		fallDetection.start();
		bc.registerService(FallDetectionService.class.getName(), fallDetection, new Hashtable());
		System.out.println("Service registered: FallDetection");
	}

	@Override
	public void stop(BundleContext bc) throws Exception
	{
		System.out.println("Stoping datebundle...");

		//stop the provided services
		fallDetection.stop();
		editor.stop();
		
		//release resources of the provided services
		fallDetection = null;
		editor = null;
		
		//release references to the required services
		bc.ungetService(bc.getServiceReference(Notification.class.getName()));
		bc.ungetService(bc.getServiceReference(SpeechService.class.getName()));
		bc.ungetService(bc.getServiceReference(ContactListService.class.getName()));
	}
}
