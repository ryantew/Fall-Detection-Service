package fall_detection.impl;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileInputStream;

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
		populateList();
	}
	
	public ArrayList<Contact> getContacts(){
		return contacts;
	}
	
	public void deleteContact(Contact item){
		contacts.remove(item);
	}
	
	public void populateList(){
		File contactsFile = new File(fileURI);
		try {
			FileInputStream stream = new FileInputStream(contactsFile);
			Scanner s = new Scanner(stream);
			while(s.hasNextLine()){
				contacts.add(parseContact(s.nextLine()));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Contact parseContact(String line){
		int commaIndex = line.indexOf(',');
		int oldIndex = commaIndex;
		String name;
		String email;
		String phone;
		if(commaIndex != 0){
			name = line.substring(0, commaIndex);
		}else{
			name = "";
		}
		commaIndex = line.indexOf(',', commaIndex);
		if(commaIndex-oldIndex > 0){
			email = line.substring(oldIndex + 1, commaIndex);
		}else{
			email = "";
		}
		if(commaIndex + 1 < line.length()){
			phone = line.substring(commaIndex + 1);
		}else{
			phone = "";
		}
		return new Contact(name, email, phone);
	}
}
