package de.sb.broker.model;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Embeddable
@Table(name="Person", schema="_s0545840__brokerDB")
public class Contact {
	@Column(name = "email", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 63, message = "An person's email address must contain between 1 and 31 characters")
	@Pattern(regexp = "^[\\w|\\d|.]+@[\\w|\\d|.]+$", message="A vaild email address is required.")
	private String email;
	
	@Column(name = "phone", updatable=true, nullable=true, insertable=true)
	@Size(min = 1, max = 31, message = "An person's phone number name must contain between 1 and 31 characters")
	private String phone;
	
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
