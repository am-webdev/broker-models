package de.sb.broker.model;

import javax.persistence.*;

@Entity()
@Table(name="Bid", schema="_s0545840__brokerDB")
@PrimaryKeyJoinColumn(referencedColumnName = "identiy")
public class Bid extends BaseEntity {
	
	@Column(name = "price", updatable=true, nullable=false, insertable=true)
	private long price; 		// in cents, min: 1  max: Long.max
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "auctionIdentity")
	private Auction auction;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "personIdentity")
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
