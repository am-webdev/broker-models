package de.sb.broker.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.glassfish.jersey.message.filtering.EntityFiltering;

import de.sb.java.validation.Inequal;

/*@Entity
@Table(name="Auction", schema="_s0545840__brokerDB")
@PrimaryKeyJoinColumn(name = "auctionIdentity")
@DiscriminatorValue("Auction")			
@Inequal(leftAccessPath = "closureTimestamp", rightAccessPath = "creationTimestamp", operator = Inequal.Operator.GREATER)
@XmlType
@XmlRootElement
@XmlAccessorType (XmlAccessType.NONE)*/

@Entity
@PrimaryKeyJoinColumn (name = "auctionIdentity")
@Table(name="Auction", schema="_s0545840__brokerDB")
@DiscriminatorValue("Auction")	
@Inequal (leftAccessPath = "closureTimestamp", rightAccessPath = "creationTimestamp", operator = Inequal.Operator.GREATER_EQUAL)
@XmlAccessorType (XmlAccessType.NONE)

public class Auction extends BaseEntity {

	@XmlElement
	@Column(name = "title", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 255)
	private String title;
	
	@XmlElement
	@Column(name = "unitCount", updatable=true, nullable=false, insertable=true)
	@Min(1)
	private short unitCount;
	
	@XmlElement
	@Column(name = "askingPrice", updatable=true, nullable=false, insertable=true)
	@Min(value = 1)
	private long askingPrice;
	
	@XmlElement
	@Column(name = "closureTimestamp", updatable=true, nullable=false, insertable=true)
	private long closureTimestamp; 		// in millisec since 1970-01-01 00-00-00-000
	
	@XmlElement
	@Column(name = "description", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 8189)
	private String description;

	@ManyToOne
	@JoinColumn(name = "sellerReference")
	private Person seller;

	
	@OneToMany(mappedBy = "auction", cascade = CascadeType.REMOVE)
	private Set<Bid> bids;
	
	protected Auction() {
		this(null);
	}
	
	public Auction(Person seller) {
		this.seller = seller;
		this.unitCount = 0;
		this.askingPrice = 0;
		this.closureTimestamp = System.currentTimeMillis() + (30*24*60*60*1000); // set to current time + 30d in millisec
		this.description = "";
		this.bids = new HashSet<Bid>();
	}

	// Getter Setter

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public short getUnitCount() {
		return this.unitCount;
	}

	public void setUnitCount(short unitCount) {
		this.unitCount = unitCount;
	}

	public long getAskingPrice() {
		return this.askingPrice;
	}

	public void setAskingPrice(long askingPrice) {
		this.askingPrice = askingPrice;
	}

	public long getClosureTimestamp() {
		return this.closureTimestamp;
	}

	public void setClosureTimestamp(long closureTimestamp) {
		this.closureTimestamp = closureTimestamp;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Bid getBid(Person bidder) {
		Bid rtn = null;
		for (Bid b : bids) {
		    if(b.getBidder() == bidder) {
		    	return b;
		    }
		}
		return rtn;
	}
	
	@XmlBidsAsEntityFilter
	@XmlElement
	public Set<Bid> getBids() {
		return this.bids;
	}
	
	
	@XmlSellerAsReferenceFilter
	@XmlElement
	public long getSellerReference() {
		return this.seller == null ? 0: this.seller.getIdentity();
	}
	
	
	@XmlSellerAsEntityFilter
	@XmlElement
	public Person getSeller() {
		return this.seller;
	}

	public void setSeller(Person seller) {
		this.seller = seller;
	}	
	
	@XmlElement
	public boolean isClosed() {
		return (System.currentTimeMillis() > this.closureTimestamp);
	}
	
	@XmlElement
	public boolean isSealed() {
		return !isClosed() && !this.getBids().isEmpty();
	}

	/**
	 * Filter annotation for associated sellers marshaled as entities.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlSellerAsEntityFilter {
		static final class Literal extends AnnotationLiteral<XmlSellerAsEntityFilter> implements XmlSellerAsEntityFilter {}
	}

	/**
	 * Filter annotation for associated sellers marshaled as references.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlSellerAsReferenceFilter {
		static final class Literal extends AnnotationLiteral<XmlSellerAsReferenceFilter> implements XmlSellerAsReferenceFilter {}
	}

	/**
	 * Filter annotation for associated bids marshaled as entities.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlBidsAsEntityFilter {
		static final class Literal extends AnnotationLiteral<XmlBidsAsEntityFilter> implements XmlBidsAsEntityFilter {}
	}

}