package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Person;

@Path("people")
public class PersonService {
	
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("broker");
	private static final EntityManager em = emf.createEntityManager();
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Person> getPeople(){
		List<Person> l;
		try{
			TypedQuery<Person> q = em.createQuery("SELECT p FROM Person p", Person.class);
			l =  q.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Person>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
	
	@PUT
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void setPerson(){
		try{
			em.getTransaction().begin();
			final Person p = new Person();
			em.persist(p);
			em.getTransaction().commit();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
	}
	
	@GET
	@Path("{identity}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Person getPeopleIdentity(@PathParam("identity") final long id){
		Person p;
		try{
			TypedQuery<Person> query = em
					.createQuery("SELECT p FROM Person p WHERE p.identity = :id", Person.class)
					.setParameter("id", id);
			p = query.getSingleResult();
		}catch(NoResultException e){
			p = new Person();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return p;
	}
	
	@GET
	@Path("{identity}/auctions")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getPeopleIdentityAuctions(@PathParam("identity") final long id){
		List<Auction> l;
		try{
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a WHERE a.seller.identity = :id OR a.bidder.identity = :id", Auction.class)
					.setParameter("id", id);
			l = query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Auction>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
	
	@GET
	@Path("{identity}/bids")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Bid> getPeopleIdentityBids(@PathParam("identity") final long id){
		//TODO: fetch join
		List<Bid> l;
		try{
			TypedQuery<Bid> query = em.createQuery("SELECT b FROM Bid b JOIN b.auction a WHERE b.bidder.identity = :id", Bid.class)
					.setParameter("id", id);
			l = query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Bid>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
}
