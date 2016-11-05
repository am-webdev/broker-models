package de.sb.broker.rest;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.broker.model.Person;

public class AuctionService {

	final static EntityManagerFactory emf = LifeCycleProvider.brokerManager().getEntityManagerFactory();
	
	// GET /auctions:
	@GET
	@Produces("application/xml")
	@Path("xml/auctions:")
	public Person getAuctionsXML(){
		
		//TODO: write return 
		return null;
		
	}
	
	// GET /auctions
	@PUT
	@Produces("application/xml")
	@Path("xml/auctions")
	public void updateAuctionsXML(){
		
		//TODO: put into DB
		
	}
	
	// GET /auctions/{identity}
	@GET
	@Produces("application/xml")
	@Path("xml/auctions/{identity}:")
	public Person[] getAuctionIdentityXML(@PathParam("identity") long identity){
		
		//TODO: write return 
		return null;
		
	}
	
}
