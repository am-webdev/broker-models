package de.sb.broker.rest;

import javax.persistence.EntityManagerFactory;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import de.sb.broker.model.Person;

public class PersonService {

	final static EntityManagerFactory emf = LifeCycleProvider.brokerManager().getEntityManagerFactory();
	
	// GET /people
	@GET
	@Produces("application/xml")
	@Path("xml/people")
	public Person getPeopleXML(){
		
		//TODO: write return 
		return null;
		
	}
	
	// GET /people/{identity}
	@GET
	@Produces("application/xml")
	@Path("xml/people/{identity}")
	public Person getPeopleIdentityXML(@PathParam("identity") long identity){
		
		//TODO: write return 
		return null;
		
	}
	
	// GET /people/{identity}/auctions
	@GET
	@Produces("application/xml")
	@Path("xml/people/{identity}/auctions")
	public Person[] getPeopleIdentityAuctionsXML(@PathParam("identity") long identity){
		
		//TODO: write return 
		return null;
		
	}
	
	// GET /people/{identity}/bids
	@GET
	@Produces("application/xml")
	@Path("xml/people/{identity}/bids")
	public Person[] getPeopleIdentityBidsXML(@PathParam("identity") long identity){
		
		//TODO: write return 
		return null;
		
	}
	
	
	
}
