package de.sb.broker.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Table(name="Person", schema="_s0545840__brokerDB")
public class Person extends BaseEntity {
	
	private String alias;
	private byte[] passwordHash;
	private Group group;
	private Name name;
	private Address address;
	private Contact contact;
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
