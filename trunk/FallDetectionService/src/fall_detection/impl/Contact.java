package fall_detection.impl;

public class Contact {
	private String name;
	private String email;
	private String phone;
	
	public Contact(String pName, String pEmail, String pPhone){
		name = pName;
		email = pEmail;
		phone = pPhone;
	}
	
	public String getName(){
		return name;
	}
	
	public String getEmail(){
		return email;
	}
	
	public String getPhoneNum(){
		return phone;
	}
	
	public void setName(String newName){
		name = newName;
	}
	
	public void setEmail(String newEmail){
		email = newEmail;
	}
	
	public void setPhone(String newPhone){
		phone = newPhone;
	}
	
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
