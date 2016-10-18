package de.sb.broker.model;

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
		this.price = 0;
	}
	
	public Auction getAuction() {
		return this.auction;
	}
	
	public long getAuctionReference() {
		return this.auction.getIdentity();
		
	}
	
	public Person getBidder() {
		return this.bidder;
		
	}
	
	public long getBidderReference(){
		return this.bidder.getIdentity();
	}
}
