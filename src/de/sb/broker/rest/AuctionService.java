package de.sb.broker.rest;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Person;

public class AuctionService {

	final static EntityManagerFactory emf = LifeCycleProvider.brokerManager().getEntityManagerFactory();
	
	@GET
	@Produces("application/xml")
	@Path("xml/auctions:")
	public <Set>Auction getAuctionsXML(){
		
		// select auction from Auction
		
		//TODO: write return 
		return null;
		
	}
	
	@PUT
	@Produces("application/xml")
	@Path("xml/auctions")
	public void updateAuctionsXML(){
		
		// update auction set @PUT variable ...
		
		//TODO: put into DB
		
	}
	
	@GET
	@Produces("application/xml")
	@Path("xml/auctions/{identity}:")
	public Auction getAuctionIdentityXML(@PathParam("identity") long identity){
		
		// select auction from Auction where auction.auctionIdentity = :identity
		
		//TODO: write return 
		return null;
		
	}
	
}
