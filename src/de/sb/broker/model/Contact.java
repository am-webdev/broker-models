package de.sb.broker.model;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Embeddable
public class Contact {
	@Column(name = "email", updatable=true, nullable=false, insertable=true)
	@Size(min = 3, max = 63)
	@Pattern(regexp = "^[\\s\\S]+@[\\s\\S]+$") 
	private String email;
	
	@Column(name = "phone", updatable=true, nullable=true, insertable=true)
	@Size(min = 0, max = 31)
	private String phone;
	
	public Contact(){
		
	}
	
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
