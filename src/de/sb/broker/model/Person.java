package de.sb.broker.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.annotations.CacheIndex;

@Entity
@Table(name="Person", schema="_s0545840__brokerDB")
@PrimaryKeyJoinColumn(name = "personIdentity")	
@DiscriminatorValue("Person")
@XmlType
@XmlRootElement
public class Person extends BaseEntity {

	public static enum Group {
		ADMIN, USER
	}
	
	@XmlElement
	@Column(name = "alias", updatable=true, nullable=false, insertable=true)
	@CacheIndex(updateable = true)
	@Size(min = 1, max = 16)
	private String alias;
	
	@Column(name = "passwordHash", updatable=true, nullable=false, insertable=true)
	@Size(min = 32, max = 32)
	private byte[] passwordHash;
	
	@XmlElement
	@Column(name = "groupAlias", updatable=true, nullable=false, insertable=true)
	@Enumerated(EnumType.STRING)
	private Group group;

	@XmlElement
	@Embedded
	@Valid 
	@NotNull
	private Name name;

	@XmlElement
	@Embedded
	@Valid 
	@NotNull
	private Address address;
	
	@XmlElement
	@Embedded
	@Valid 
	@NotNull
	private Contact contact;
	
	@ManyToOne(cascade = CascadeType.PERSIST) 
	/*
	 * TODO tests are failing on cascade = CascadeType.DETACH
	 * Error on tests:
	 * 	java.lang.IllegalStateException: 
	 * 	During synchronization a new object was found through a relationship that was not marked cascade PERSIST:
	 * 	de.sb.broker.model.Document@178270b2.
	 */
	@JoinColumn(name = "avatarReference")
	private Document avatar;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seller")
	@NotNull
	private Set<Auction> auctions;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bidder")
	@NotNull
	private Set<Bid> bids;
	
	public Person() {
		this.alias = " ";
		this.passwordHash = null;
		this.group = Group.USER;
		this.name = new Name();
		this.address = new Address();
		this.contact = new Contact();
		this.auctions = new HashSet<Auction>();
		this.bids = new HashSet<Bid>();
		this.avatar = null;
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

	public Set<Auction> getAuctions() {
		return auctions;
	}

	public Set<Bid> getBids() {
		return bids;
	}

	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}
	
	@Override
	public String toString() {
		return "Person [identity="+ getIdentity() + "alias=" + alias + ", passwordHash=" + Arrays.toString(passwordHash) + ", group=" + group
				+ ", name=" + name + ", address=" + address + ", contact=" + contact + ", avatar=" + avatar
				+ ", auctions=" + auctions + ", bids=" + bids +"]";
	}
}
