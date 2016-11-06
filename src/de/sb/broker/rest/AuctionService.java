package de.sb.broker.rest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;

@Path("auctions")
public class AuctionService {

//	static EntityManager em = LifeCycleProvider.brokerManager();
//	static EntityManagerFactory emf = em.getEntityManagerFactory();
//	static CriteriaBuilder cb = em.getCriteriaBuilder();
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public <Set>Auction getAuctionsXML(){
		
		// select auction from Auction
		
		//TODO: write return 
		return null;
		
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public void updateAuctionsXML(){
		
		// update auction set @PUT variable ...
		
		//TODO: put into DB
		
	}
	
	@GET
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	public Auction getAuctionIdentityXML(@PathParam("identity") final long identity){
		
		// select auction from Auction where auction.auctionIdentity = :identity
		
		//TODO: write return 
		return null;
		
	}
	
}
