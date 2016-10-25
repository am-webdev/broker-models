package de.sb.broker.model;

import javax.persistence.*;

@Embeddable
@Table(name="Person", schema="_s0545840__brokerDB")
public class Contact {
	@Column(name = "email", updatable=true, nullable=false, insertable=true)
	private String email;
	
	@Column(name = "phone", updatable=true, nullable=true, insertable=true)
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
