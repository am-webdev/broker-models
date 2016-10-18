package de.sb.broker.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Auction extends BaseEntity {
	private String title;
	private short unitCount;
	private long askingPrice;
	private long closureTimestamp; 		// in millisec since 1970-01-01 00-00-00-000
	private String description;
	private Person seller;
	private Map<Person, Bid> bids;
	
	protected Auction() {
		this(null);
	}
	
	public Auction(Person seller) {
		this.seller = seller;
		this.unitCount = 0;
		this.askingPrice = 0;
		this.closureTimestamp = 42;
		this.description = "";
		this.bids = new HashMap<Person, Bid>();
	}
	
	public Person getSeller() {
		return this.seller;
	}
	
	public long getSellerReference() {

		return this.seller.getIdentity();
	}
	
	public Bid getBid(Person bidder) {
		return this.bids.get(bidder);
	}
	
	public boolean isClosed() {
		return (System.currentTimeMillis() > this.closureTimestamp);
	}
	
	public boolean isSealed() {
		return (this.bids.size() > 0 || isClosed());
	}
	
}
