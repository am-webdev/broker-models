package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Person;

@Path("people")
public class PersonService {
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Person> getPeople(){
		final EntityManager em = LifeCycleProvider.brokerManager();
		TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p", Person.class);
		return query.getResultList();
	}
	
	@GET
	@Path("{identity}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Person getPeopleIdentity(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		TypedQuery<Person> query = em
				.createQuery("SELECT p FROM Person p WHERE p.identity = :id", Person.class)
				.setParameter("id", id);
		return query.getSingleResult();
	}
	
	@GET
	@Path("{identity}/auctions")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getPeopleIdentityAuctions(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a WHERE a.seller.identity = :id", Auction.class)
				.setParameter("id", id);
		return query.getResultList();
	}
	
	@GET
	@Path("{identity}/bids")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Bid> getPeopleIdentityBids(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		TypedQuery<Bid> query = em.createQuery("SELECT b from Bid b WHERE b.bidder.identity = :id", Bid.class)
				.setParameter("id", id);
		return query.getResultList();
	}
}
