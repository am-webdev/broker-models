package de.sb.broker.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.glassfish.jersey.message.filtering.EntityFiltering;

import de.sb.java.validation.Inequal;

/*@Entity()
@Table(name="Bid", schema="_s0545840__brokerDB")
@PrimaryKeyJoinColumn(name = "bidIdentity")
@DiscriminatorValue("Bid")			
@Inequal(leftAccessPath = "price", rightAccessPath = { "auction", "askingPrice" } , operator = Inequal.Operator.GREATER_EQUAL )
@Inequal(leftAccessPath = { "bidder" , "identity" }, rightAccessPath = { "auction", "seller" , "identity" } , operator = Inequal.Operator.NOT_EQUAL)
@XmlType
@XmlRootElement
@XmlAccessorType (XmlAccessType.NONE)*/

@Entity
@PrimaryKeyJoinColumn (name = "bidIdentity")
@Table(name="Bid", schema="_s0545840__brokerDB")
@DiscriminatorValue("Bid")	
@Inequal (leftAccessPath = "price", rightAccessPath = { "auction", "askingPrice" }, operator = Inequal.Operator.GREATER_EQUAL)
@Inequal (leftAccessPath = { "bidder", "identity" }, rightAccessPath = { "auction", "seller", "identity" })
@XmlAccessorType (XmlAccessType.NONE)

public class Bid extends BaseEntity {
	
	@XmlElement
	@Column(name = "price", updatable=true, nullable=false, insertable=true)
	@Min(1)
	private long price; 		// in cents, min: 1  max: Long.max
	
	@ManyToOne
	//@JoinColumn(name = "auctionIdentity")
	@JoinColumn(name = "auctionReference")
	private Auction auction;
	
	@ManyToOne
	//@JoinColumn(name = "personIdentity")
	@JoinColumn(name = "bidderReference")
	private Person bidder;
	
	protected Bid() {
		this(null, null);
	}
	
	public Bid(Auction auction, Person bidder) {
		this.auction = auction;
		this.bidder = bidder;
		this.price = auction == null ? 1: auction.getAskingPrice();
	}
	
	@XmlAuctionAsEntityFilter
	@XmlElement
	public Auction getAuction() {
		return this.auction;
	}
	
	@XmlAuctionAsReferenceFilter
	@XmlElement
	public long getAuctionReference() {
		return this.auction == null ? 0: this.auction.getIdentity();
		
	}
	
	@XmlBidderAsEntityFilter
	@XmlElement
	public Person getBidder() {
		return this.bidder;
		
	}

	@XmlBidderAsReferenceFilter
	@XmlElement
	public long getBidderReference(){
		return this.bidder == null ? 0: this.bidder.getIdentity();
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	
	//TODO fix all imports
	/**
	 * Filter annotation for associated bidders marshaled as entities.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlBidderAsEntityFilter {
		static final class Literal extends AnnotationLiteral<XmlBidderAsEntityFilter> implements XmlBidderAsEntityFilter {}
	}

	/**
	 * Filter annotation for associated bidders marshaled as references.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlBidderAsReferenceFilter {
		static final class Literal extends AnnotationLiteral<XmlBidderAsReferenceFilter> implements XmlBidderAsReferenceFilter {};
	}

	/**
	 * Filter annotation for associated auctions marshaled as entities.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlAuctionAsEntityFilter {
		static final class Literal extends AnnotationLiteral<XmlAuctionAsEntityFilter> implements XmlAuctionAsEntityFilter {}
	}

	/**
	 * Filter annotation for associated auctions marshaled as references.
	 */
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	@EntityFiltering
	@SuppressWarnings("all")
	static public @interface XmlAuctionAsReferenceFilter {
		static final class Literal extends AnnotationLiteral<XmlAuctionAsReferenceFilter> implements XmlAuctionAsReferenceFilter {}
	}
}