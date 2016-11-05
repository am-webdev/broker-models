package de.sb.broker.rest;

import java.io.File;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Selection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.sb.broker.model.Person;

public class PersonService {

	final static EntityManager em = LifeCycleProvider.brokerManager();
	final static EntityManagerFactory emf = em.getEntityManagerFactory();
	final static CriteriaBuilder cb = em.getCriteriaBuilder();	
	
//    JAXBContext jc = JAXBContext.;
//    Unmarshaller u = jc.createUnmarshaller();
//    Marshaller m = jc.createMarshaller();
	
	@GET
	@Produces("application/xml")
	@Path("xml/people")
	public <Set>Person getPeopleXML(){

//		Selection<Criteria> s = new Selection();
//		CriteriaQuery cq = cb.createQuery();
//		cq.select(arg0)
		
		//select person from Person
		
		//emf.addNamedQuery("people", arg1);
		
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
