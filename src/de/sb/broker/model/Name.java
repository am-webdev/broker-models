package de.sb.broker.model;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Embeddable
@Table(name="Person", schema="_s0545840__brokerDB")
public class Name {

	@Column(name = "familyName", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 31, message = "An person's family name must contain between 1 and 31 characters")
	private String family;
	
	@Column(name = "givenName", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 31, message = "An person's given name must contain between 1 and 31 characters")
	private String given;
	
	public String getFamily() {
		return this.family;
	}
	public void setFamily(String family) {
		this.family = family;
	}
	public String getGiven() {
		return this.given;
	}
	public void setGiven(String given) {
		this.given = given;
	}

}
