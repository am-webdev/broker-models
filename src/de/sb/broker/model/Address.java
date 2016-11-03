package de.sb.broker.model;

import javax.persistence.Column;
import javax.validation.constraints.Size;
import javax.persistence.*;

@Embeddable
public class Address {

	@Column(name = "street", updatable=true, nullable=true, insertable=true)
	@Size(min = 0, max = 63)
	private String street;
	
	@Column(name = "postCode", updatable=true, nullable=true, insertable=true)
	@Size(min = 0, max = 15)
	private String postCode;
	
	@Column(name = "city", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 63)
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
