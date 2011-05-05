package contact_editor.impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import contact_editor.IContactEditor;
import fall_detection.ContactListService;
import fall_detection.impl.Contact;
import fall_detection.impl.ContactList;
import fall_detection.impl.FallDetection;

/**
 * 
 * The contact editor service.
 * 
 * @author Ryan Tew
 * @author John George
 *
 */
public class ContactEditor implements IContactEditor
{
	/**
	 * The frame for the service
	 */
	private JFrame frame;
	
	/**
	 * The text fields for the contact
	 */
	private JTextField contactName;
	private JTextField contactEmail;
	private JTextField contactPhone;
	
	/**
	 * The list of contacts
	 */
	private JList contactList;
	
	/**
	 * Background datastructure for holding the list of contacts
	 */
	private ContactListService contacts;
	
	/**
	 * Reference the contact being edited
	 */
	private Contact currentContact;
	
	/**
	 * Test function
	 * @param args
	 */
	public static void main(String args[])
	{
		ContactEditor contactEditor = new ContactEditor(new ContactList(FallDetection.CONTACT_FILE));
		contactEditor.start();
	}

	/**
	 * Sets up the required information for the service.
	 * This constructor creates the background file for storing the contacts.
	 */
	public ContactEditor(ContactListService contacts)
	{
		this.contacts = contacts;
	}

	@Override
	public void start()
	{
		//initialize the window and populate its lists
		initializeComponents();
		currentContact = null;
		displayContacts();
	}
	
	@Override
	public void stop()
	{
		//destroy the window
		frame.dispose();
	}

	/**
	 * Initialize the components of the window.
	 */
	private void initializeComponents()
	{
		frame = new JFrame();
		Container container = frame.getContentPane();

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(500, 500));

		//create the button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3));
		container.add(buttonPanel, BorderLayout.SOUTH);
		
		JButton save = new JButton("save");
		save.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveContact();
			}
		});
		
		JButton edit = new JButton("edit");
		edit.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				editContact();
			}
		});
		
		JButton delete = new JButton("delete");
		delete.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				deleteContact();
			}
		});
		
		buttonPanel.add(save);
		buttonPanel.add(edit);
		buttonPanel.add(delete);

		//create the text panel
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new GridLayout(1, 6, 10, 10));
		container.add(textPanel, BorderLayout.NORTH);
		
		contactName = new JTextField();
		contactEmail = new JTextField();
		contactPhone = new JTextField();
		
		textPanel.add(new JLabel("Name:"));
		textPanel.add(contactName);
		textPanel.add(new JLabel("Email:"));
		textPanel.add(contactEmail);
		textPanel.add(new JLabel("Phone:"));
		textPanel.add(contactPhone);

		//create the contact list
		contactList = new JList();
		
		container.add(contactList, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Pulls the information for a contact out of the list and allows you to edit it.
	 */
	private void editContact()
	{
		//get the selected contact
		int sel = contactList.getSelectedIndex();
		if(sel == -1){
			return;
		}
		currentContact = contacts.getContacts().get(sel);

		//populate the text fields
		contactName.setText(currentContact.getName());
		contactEmail.setText(currentContact.getEmail());
		contactPhone.setText(currentContact.getPhoneNum());
		
		displayContacts();
	}
	
	private void saveContact()
	{
		//get the information out of the text fields
		String name = contactName.getText();
		String email = contactEmail.getText();
		String phone = contactPhone.getText();

		//take commas out of the fields
		name.replace(",", "");
		email.replace(",", "");
		phone.replace(",", "");

		//check for the domain in the phone and email
		if((!email.equals("") && !email.contains("@")) || (!phone.equals("") && !phone.contains("@")))
		{
			error();
			return;
		}

		//save the contact
		if(currentContact != null)
		{
			currentContact.setName(name);
			currentContact.setEmail(email);
			currentContact.setPhone(phone);
			currentContact = null;
		}
		else
		{
			contacts.add(new Contact(name, email, phone));
		}

		//save to the file
		contacts.writeToFile();
		
		displayContacts();
	}
	
	/**
	 * Displays an error message
	 */
	private void error()
	{
		Object[] options = {"OK"};
		int n = JOptionPane.showOptionDialog(frame,
				"You must enter a domain for emails and phones",
				"Error",
				JOptionPane.ERROR_MESSAGE,
				JOptionPane.ERROR_MESSAGE,
				null,
				options,
				options[0]);
	}

	/**
	 * Deletes a contact
	 */
	private void deleteContact()
	{
		//get the selected index
		int sel = contactList.getSelectedIndex();
		if(sel == -1)
			return;

		//delete the contact
		contacts.deleteContact(contacts.getContacts().get(sel));
		
		//save to the file
		contacts.writeToFile();
		
		displayContacts();
	}
	
	/**
	 * Updates the contacts in the list
	 */
	private void displayContacts()
	{
		contactList.setListData(contacts.getContacts().toArray());
	}
}
