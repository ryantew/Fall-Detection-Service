package fall_detection;

import java.util.ArrayList;

import fall_detection.impl.Contact;

public interface ContactListService
{
	/**
	 * Gets the ArrayList of the contacts
	 * @return the list of contacts
	 */
	public ArrayList<Contact> getContacts();

	/**
	 * Removes a contact from the list.
	 * @param item the contact to be deleted
	 */
	public void deleteContact(Contact item);

	/**
	 * Adds a contact to the list
	 * @param c the contact to be added
	 */
	public void add(Contact c);

	/**
	 * Refreshes the internal list, correlating it with the contact list file
	 */
	public void populateList();

	/**
	 * Writes the contacts in the internal list to the file after clearing the file.
	 */
	public void writeToFile();
}
