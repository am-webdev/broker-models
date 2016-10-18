package de.sb.broker.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Person extends BaseEntity {
	
	private String alias;
	private byte[] passwordHash;
	private Group group;
	private Name name;
	private Address address;
	private Contact contact;
	private List<Auction> auctions;
	private List<Bid> bids;
	
	public static enum Group {
		ADMIN, USER
	}
	
	public Person() {
		this.alias = "";
		this.passwordHash = null;
		this.group = Group.USER;
		this.address = null;
		this.contact = null;
		this.auctions = new ArrayList<Auction>();
		this.bids = new ArrayList<Bid>();
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

	public List<Auction> getAuctions() {
		return auctions;
	}

	public List<Bid> getBids() {
		return bids;
	}
	
	
	
}
