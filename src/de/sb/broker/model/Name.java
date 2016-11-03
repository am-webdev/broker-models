package de.sb.broker.model;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Embeddable
public class Name {
	
	@Column(name = "familyName", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 31)
	private String family;
	
	@Column(name = "givenName", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 31)
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
