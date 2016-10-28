package de.sb.broker.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Size;

@Entity
@Table(name="Person", schema="_s0545840__brokerDB")
public class Person extends BaseEntity {
	
	@Column(name = "alias", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 16, message = "An person's alias must contain between 1 and 16 characters")
	private String alias;
	
	@Column(name = "passwordHash", updatable=true, nullable=false, insertable=true)
	@Size(min = 32, max = 32, message = "The passwordhash has to contain 32 bytes")
	private byte[] passwordHash;
	
	@Column(name = "groupAlias", updatable=true, nullable=false, insertable=true)
	@Enumerated(EnumType.STRING)
	private Group group;

	@Embedded
	@Valid 
	private Name name;

	@Embedded
	@Valid 
	private Address address;
	
	@Embedded
	@Valid 
	private Contact contact;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seller")
	private HashSet<Auction> auctions;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bidder")
	private HashSet<Bid> bids;
	
	public static enum Group {
		ADMIN, USER
	}
	
	public Person() {
		this.alias = "";
		this.passwordHash = null;
		this.group = Group.USER;
		this.address = null;
		this.contact = null;
		this.auctions = new HashSet<Auction>();
		this.bids = new HashSet<Bid>();
	}
	
	public static byte[] passwordHash(String password) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return digest.digest(password.getBytes(StandardCharsets.UTF_8));
	}
	
	// Getter Setter

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public byte[] getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public Name getName() {
		return name;
	}

	public void setName(Name name) {
		this.name = name;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Contact getContact() {
		return contact;
	}

	public void setContact(Contact contact) {
		this.contact = contact;
	}

	public HashSet<Auction> getAuctions() {
		return auctions;
	}

	public HashSet<Bid> getBids() {
		return bids;
	}
	
	
	
}
