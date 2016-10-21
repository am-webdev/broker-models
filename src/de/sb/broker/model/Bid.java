package de.sb.broker.model;

import javax.persistence.Table;

@Table(name="Bid", schema="_s0545840__brokerDB")
public class Bid extends BaseEntity {
	private long price; 		// in cents
	private Auction auction;
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
