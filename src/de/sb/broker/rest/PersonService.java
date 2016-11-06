package de.sb.broker.rest;


import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Person;

@Path("people")
public class PersonService {

//	static EntityManager em = LifeCycleProvider.brokerManager();
//	static EntityManagerFactory emf = em.getEntityManagerFactory();
//	static CriteriaBuilder cb = em.getCriteriaBuilder();	
	
//    JAXBContext jc = JAXBContext.;
//    Unmarshaller u = jc.createUnmarshaller();
//    Marshaller m = jc.createMarshaller();
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<Person> getPeopleXML(){
		
		List<Person> l = new ArrayList<Person>();
		l.add(new Person());
		l.add(new Person());
		l.add(new Person());

//		Selection<Criteria> s = new Selection();
//		CriteriaQuery cq = cb.createQuery();
//		cq.select(arg0)
		
		//select person from Person
		
		//emf.addNamedQuery("people", arg1);
		
		//TODO: write return 
		return l;
		
	}
	
	@GET
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	public Person getPeopleIdentityXML(@PathParam("identity") final long identity){
		
		// select person from Person where person.identity = :personIdentity
		
		//TODO: write return 
		return null;
		
	}
	
	@GET
	@Path("{identity}/auctions")
	@Produces(MediaType.APPLICATION_XML)
	public List<Person> getPeopleIdentityAuctionsXML(@PathParam("identity") final long identity){
		
		// select auctions from Auctions where person.identity = :personIdentity 
		
		//TODO: write return 
		return null;
		
	}
	
	@GET
	@Path("{identity}/bids")
	@Produces(MediaType.APPLICATION_XML)
	public List<Person> getPeopleIdentityBidsXML(@PathParam("identity") final long identity){
		
		// select bid from Bid where bid.bidderReference = :personIdentity
		
		//TODO: write return 
		return null;
		
	}
	
	
	
}
