package fall_detection.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author John George
 *
 */
public class ContactList {

	private ArrayList<Contact> contacts;
	private String fileURI;
	
	public ContactList(String URI){
		fileURI = URI;
		contacts = new ArrayList<Contact>();
		populateList();
	}
	
	public ArrayList<Contact> getContacts(){
		return contacts;
	}
	
	public void deleteContact(Contact item){
		contacts.remove(item);
	}
	
	public void add(Contact c){
		for(Contact contact : contacts)
		{
			if(contact.equals(c))
			{
				return;
			}
		}
		
		contacts.add(c);
	}
	
	public void populateList(){
		File contactsFile = new File(fileURI);
		try {
			FileInputStream stream = new FileInputStream(contactsFile);
			Scanner s = new Scanner(stream);
			contacts = new ArrayList<Contact>();
			while(s.hasNextLine()){
				contacts.add(parseContact(s.nextLine()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToFile(){
		File f = new File(fileURI);
		FileWriter stream;
		try {
			f.createNewFile();
			stream = new FileWriter(f);
			
			for(Contact c : contacts){
				stream.write(c.fileToString());
			}
			
			stream.flush();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}
	
	private Contact parseContact(String line){
		String[] info = line.split(",");
		String name = "";
		String email = "";
		String phone = "";
		
		if(info.length > 0)
			name = info[0];
		if(info.length > 1)
			email = info[1];
		if(info.length > 2)
			phone = info[2];
		
		return new Contact(name, email, phone);
	}
}
