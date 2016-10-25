package de.sb.broker.model;

import javax.persistence.Column;
import javax.persistence.*;

@Embeddable
@Table(name="Person", schema="_s0545840__brokerDB")
public class Address {

	@Column(name = "street")
	private String street;
	@Column(name = "postCode")
	private String postCode;
	@Column(name = "city")
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
