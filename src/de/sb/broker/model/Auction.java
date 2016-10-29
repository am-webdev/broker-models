package de.sb.broker.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import de.sb.java.validation.Inequal;

@Entity
@Table(name="Auction", schema="_s0545840__brokerDB")
@Inequal(leftAccessPath = "closureTimestamp", rightAccessPath = "creationTimestamp", operator = Inequal.Operator.GREATER)
public class Auction extends BaseEntity {
	@Column(name = "title", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 255, message = "The title should contain at least 1 and maximal 255 characters")
	private String title;
	
	@Column(name = "unitCount", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 99999, message = "The unit count needs to be between 1 and 99999")
	private short unitCount;
	
	@Column(name = "askingPrice", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, message = "The price needs to start at 1ct")
	private long askingPrice;
	
	@Column(name = "closureTimestamp", updatable=true, nullable=false, insertable=true)
	private long closureTimestamp; 		// in millisec since 1970-01-01 00-00-00-000
	
	@Column(name = "description", updatable=true, nullable=false, insertable=true)
	@Size(min = 1, max = 8189, message = "An auction's name must contain between 1 and 8189 characters")
	private String description;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sellerReference")
	private Person seller;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "auction")
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
