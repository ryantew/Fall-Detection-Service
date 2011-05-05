package contact_editor.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import fall_detection.ContactListService;

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
	
	@Override
	public void start(BundleContext bc) throws Exception
	{
		System.out.println(bc.getBundle().getHeaders().get(Constants.BUNDLE_NAME) + " starting...");
			
		//get the required services
		ContactListService contactListService = (ContactListService)bc.getService(bc.getServiceReference(ContactListService.class.getName()));

		//start the contact editor
		editor = new ContactEditor(contactListService);
		editor.start();
	}

	@Override
	public void stop(BundleContext bc) throws Exception
	{
		System.out.println("Stoping datebundle...");

		//stop the provided services
		editor.stop();
		
		//release resources of the provided services
		editor = null;
		
		//release references to the required services
		bc.ungetService(bc.getServiceReference(ContactListService.class.getName()));
	}
}
