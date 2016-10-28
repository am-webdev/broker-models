package de.sb.broker.model;

import javax.persistence.Column;
import javax.validation.constraints.Size;
import javax.persistence.*;

@Embeddable
public class Address {

	@Column(name = "street", updatable=true, nullable=true, insertable=true)
	@Size(min = 0, max = 63, message = "An person's street must contain between 0 and 63 characters")
	private String street;
	
	@Column(name = "postCode", updatable=true, nullable=true, insertable=true)
	@Size(min = 0, max = 15, message = "An person's post code must contain between 0 and 15 characters")
	private String postCode;
	
	@Column(name = "city", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 63, message = "An person's city name must contain between 1 and 63 characters")
	private String city;
	
	public String getStreet() {
		return this.street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getPostCode() {
		return this.postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}
	public String getCity() {
		return this.city;
	}
	public void setCity(String city) {
		this.city = city;
	}
}
