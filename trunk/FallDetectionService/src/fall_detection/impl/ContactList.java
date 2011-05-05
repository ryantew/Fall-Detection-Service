package fall_detection.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import fall_detection.ContactListService;

/**
 * This class represents the list of contacts in the file
 * @author John George
 * @author Ryan Tew
 *
 */
public class ContactList implements ContactListService {

	private ArrayList<Contact> contacts;
	private String fileURI;
	
	/**
	 * Constructs a new ContactList
	 * @param URI The path to the contact list file.
	 */
	public ContactList(String URI){
		fileURI = URI;

		//get a reference to the file
		File contactFile = new File(fileURI);
		
		if(!contactFile.exists())
		{
			//the file does not exist so make sure the folder exits
			File contactFolder = new File(fileURI.substring(0, fileURI.lastIndexOf('/')));
			contactFolder.mkdirs();
			
			try
			{
				//now create the file
				contactFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		contacts = new ArrayList<Contact>();
		populateList();
	}
	
	@Override
	public ArrayList<Contact> getContacts(){
		return contacts;
	}
	
	@Override
	public void deleteContact(Contact item){
		contacts.remove(item);
	}
	
	@Override
	public void add(Contact c){
		
		//Check to make sure the contact isn't already in the list.
		for(Contact contact : contacts)
		{
			if(contact.equals(c))
			{
				return;
			}
		}
		
		contacts.add(c);
	}
	
	@Override
	public void populateList(){
		File contactsFile = new File(fileURI);
		try {
			
			//Get the file stream
			FileInputStream stream = new FileInputStream(contactsFile);
			Scanner s = new Scanner(stream);
			
			//Flush the old contact list and fill a new one with contacts from the file.
			contacts = new ArrayList<Contact>();
			while(s.hasNextLine()){
				contacts.add(parseContact(s.nextLine()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeToFile(){
		
		//Get the file and a stream to write to it
		File f = new File(fileURI);
		FileWriter stream;
		
		
		try {
			
			//Create a new empty contact file
			f.createNewFile();
			
			stream = new FileWriter(f);
			
			//Write the contacts in the list to the file.
			for(Contact c : contacts){
				stream.write(c.fileToString());
			}
			
			//Flush and close the stream
			stream.flush();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Parses the contact from one line of the contact list file
	 * @param line The line to extract the contact from
	 * @return A new contact created from the information in the text.
	 */
	private Contact parseContact(String line){
		
		//Set up the name, email, and phone strings.
		String name = "";
		String email = "";
		String phone = "";
		
		//Split the string by commas
		String[] info = line.split(",");

		//Set the name, email, and phone to the appropriate parts of the split string
		if(info.length > 0)
			name = info[0];
		if(info.length > 1)
			email = info[1];
		if(info.length > 2)
			phone = info[2];
		
		return new Contact(name, email, phone);
	}
}
