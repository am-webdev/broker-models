package de.sb.broker.model;

import javax.persistence.*;

@Embeddable
@Table(name="Person", schema="_s0545840__brokerDB")
public class Name {

	@Column(name = "familyName", updatable=true, nullable=false, insertable=true)
	private String family;
	@Column(name = "givenName", updatable=true, nullable=false, insertable=true)
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
