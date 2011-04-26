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

	private ArrayList<String> contacts;
	private String fileURI;
	
	public ContactList(String URI){
		fileURI = URI;
		populateList();
	}
	
	public ArrayList<String> getContacts(){
		return contacts;
	}
	
	private void populateList(){
		File contactsFile = new File(fileURI);
		try {
			FileInputStream stream = new FileInputStream(contactsFile);
			Scanner s = new Scanner(stream);
			while(s.hasNextLine()){
				contacts.add(s.nextLine());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
