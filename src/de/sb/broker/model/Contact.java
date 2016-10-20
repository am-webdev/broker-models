package de.sb.broker.model;

public class Contact {
	private String email;
	private String phone;
	
	public Contact(String email, String phone) {
		this.email = email;
		this.phone = phone;
	}
	
	public String getEmail() {
		return this.email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return this.phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
}
