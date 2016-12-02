package de.sb.broker.model;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import de.sb.java.validation.Inequal;

@Entity()
@Table(name="Bid", schema="_s0545840__brokerDB")
@PrimaryKeyJoinColumn(name = "bidIdentity")
@DiscriminatorValue("Bid")			
@Inequal(leftAccessPath = "price", rightAccessPath = { "auction", "askingPrice" } , operator = Inequal.Operator.GREATER_EQUAL )
@Inequal(leftAccessPath = { "bidder" , "identity" }, rightAccessPath = { "auction", "seller" , "identity" } , operator = Inequal.Operator.NOT_EQUAL)
@XmlType
@XmlRootElement
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
	
	public Auction getAuction() {
		return this.auction;
	}
	
	public long getAuctionReference() {
		return this.auction == null ? 0: getIdentity();
		
	}
	
	public Person getBidder() {
		return this.bidder;
		
	}
	
	public long getBidderReference(){
		return this.bidder == null ? 0: getIdentity();
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}
	
}
