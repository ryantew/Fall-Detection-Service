package fall_detection.impl;

/**
 * This class represents a single emergency contact.
 * @author Ryan Tew
 * @author John George
 */
public class Contact {
	//The contact information strings
	private String name;
	private String email;
	private String phone;
	
	/**
	 * Basic constructor
	 * @param pName The contact's name
	 * @param pEmail The contact's email address
	 * @param pPhone The contact's phone number with their service's text domain
	 */
	public Contact(String pName, String pEmail, String pPhone){
		name = pName;
		email = pEmail;
		phone = pPhone;
	}
	
	/**
	 * Gets the name of this contact
	 * @return the name of this contact
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Gets the email address of this contact
	 * @return the email address of this contact
	 */
	public String getEmail(){
		return email;
	}
	
	/**
	 * Gets the phone number with text message info of this contact
	 * @return the phone number with text message info
	 */
	public String getPhoneNum(){
		return phone;
	}
	
	/**
	 * Sets the name of this contact
	 * @param newName the desired new name for this contact
	 */
	public void setName(String newName){
		name = newName;
	}
	
	/**
	 * Sets the email address of this contact.
	 * @param newEmail the desired email address
	 */
	public void setEmail(String newEmail){
		email = newEmail;
	}
	
	/**
	 * Sets the phone number of this contact
	 * @param newPhone the desired phone number
	 */
	public void setPhone(String newPhone){
		phone = newPhone;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		
		Contact c = (Contact)o;
		
		return this.name.equals(c.name) && this.email.equals(c.email) && this.phone.equals(c.phone);
	}
	
	@Override
	public String toString()
	{
		return name + ",\t" + email + ",\t" + phone;
	}
	
	public String fileToString(){
		return name + "," + email + "," + phone +"\n";
	}
}
