package de.sb.broker.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Auction extends BaseEntity {
	private String title;
	private short unitCount;
	private long askingPrice;
	private long closureTimestamp; 		// in millisec since 1970-01-01 00-00-00-000
	private String description;
	private Person seller;
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
	
	public Person getSeller() {
		return this.seller;
	}
	
	public long getSellerReference() {
		return this.seller == null ? 0: this.seller.getIdentity();
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
	
	public boolean isClosed() {
		return (System.currentTimeMillis() > this.closureTimestamp);
	}
	
	public boolean isSealed() {
		return (this.bids.size() > 0 || isClosed());
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

	public Set<Bid> getBids() {
		return this.bids;
	}

	public void setSeller(Person seller) {
		this.seller = seller;
	}	
}
