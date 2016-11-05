package de.sb.broker.rest;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.broker.model.Person;

public class PersonService {

	final static EntityManagerFactory emf = LifeCycleProvider.brokerManager().getEntityManagerFactory();
	
	@GET
	@Produces("application/xml")
	@Path("xml/people")
	public <Set>Person getPeopleXML(){
		
		//select person from Person
		
		//TODO: write return 
		return null;
		
	}
	
	@GET
	@Produces("application/xml")
	@Path("xml/people/{identity}")
	public Person getPeopleIdentityXML(@PathParam("identity") long identity){
		
		// select person from Person where person.identity = :personIdentity
		
		//TODO: write return 
		return null;
		
	}
	
	@GET
	@Produces("application/xml")
	@Path("xml/people/{identity}/auctions")
	public <Set>Person getPeopleIdentityAuctionsXML(@PathParam("identity") long identity){
		
		// select auctions from Auctions where person.identity = :personIdentity 
		
		//TODO: write return 
		return null;
		
	}
	
	@GET
	@Produces("application/xml")
	@Path("xml/people/{identity}/bids")
	public <Set>Person getPeopleIdentityBidsXML(@PathParam("identity") long identity){
		
		// select bid from Bid where bid.bidderReference = :personIdentity
		
		//TODO: write return 
		return null;
		
	}
	
	
	
}
