package fall_detection.impl;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fall_detection.IContactEditor;

public class ContactEditor implements IContactEditor
{
	
	private JFrame frame;
	
	private JTextField contactName;
	private JTextField contactEmail;
	private JTextField contactPhone;
	
	private JList contactList;
	
	private ContactList contacts;
	
	private Contact currentContact;
	
	public static void main(String args[])
	{
		ContactEditor contactEditor = new ContactEditor();
		contactEditor.start();
	}
	
	public ContactEditor()
	{
		File contactFile = new File(FallDetectionService.CONTACT_FILE);
		
		if(!contactFile.exists())
		{
			try
			{
				contactFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void start()
	{
		initializeComponents();
		contacts = new ContactList(FallDetectionService.CONTACT_FILE);
		currentContact = null;
		displayContacts();
	}
	
	private void initializeComponents()
	{
		frame = new JFrame();
		Container container = frame.getContentPane();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(500, 500));
		
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
		
		contactList = new JList();
		
		container.add(contactList, BorderLayout.CENTER);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	private void editContact()
	{
		int sel = contactList.getSelectedIndex();
		if(sel == -1){
			return;
		}
		currentContact = contacts.getContacts().get(sel);
		
		contactName.setText(currentContact.getName());
		contactEmail.setText(currentContact.getEmail());
		contactPhone.setText(currentContact.getPhoneNum());
		
		displayContacts();
	}
	
	private void saveContact()
	{
		String name = contactName.getText();
		String email = contactEmail.getText();
		String phone = contactPhone.getText();
		
		name.replace(",", "");
		
		if((!email.equals("") && !email.contains("@")) || (!phone.equals("") && !phone.contains("@")))
		{
			error();
			return;
		}
		
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
		
		contacts.writeToFile();
		
		displayContacts();
	}
	
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
	
	private void deleteContact()
	{
		int sel = contactList.getSelectedIndex();
		if(sel == -1)
			return;
		
		contacts.deleteContact(contacts.getContacts().get(sel));
		
		contacts.writeToFile();
		
		displayContacts();
	}
	
	private void displayContacts()
	{
		contactList.setListData(contacts.getContacts().toArray());
	}
}
