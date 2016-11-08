package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;

import de.sb.broker.model.Address;
import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Contact;
import de.sb.broker.model.Name;
import de.sb.broker.model.Person;
import de.sb.broker.model.Document;

@Path("people")
public class PersonService {
	
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("broker");
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Person> getPeople(){
		final EntityManager em = emf.createEntityManager();
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public long setPerson(
			@FormParam("identity") final long id,
			@FormParam("alias") final String alias,
			@FormParam("familyName") final String familyName,
			@FormParam("givenName") final String givenName,
			@FormParam("street") final String street,
			@FormParam("postCode") final String postCode,
			@FormParam("city") final String city,
			@FormParam("email") final String email,
			@FormParam("phone") final String phone,
			@HeaderParam("Set-password") final String pw
			){
		final EntityManager em = emf.createEntityManager();
		try{
			em.getTransaction().begin();
			final Person p;
			if(id != 0){
				TypedQuery<Person> q = em
						.createQuery("SELECT p FROM Person p WHERE p.identity = :id", Person.class)
						.setParameter("id", id);
				p = q.getSingleResult();
			}else{
				p = new Person();
			}
			p.setAlias(alias);
			p.setName(new Name(familyName, givenName));
			p.setAddress(new Address(street, postCode, city));
			p.setContact(new Contact(email, phone));
			p.setPasswordHash(Person.passwordHash(pw));
			em.persist(p);
			em.getTransaction().commit();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return id;
	}
	
	@GET
	@Path("{identity}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Person getPeopleIdentity(@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
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
		final EntityManager em = emf.createEntityManager();
		List<Auction> l;
		//TODO: fetch join 
		try{
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a WHERE a.seller.identity = :id", Auction.class) //TODO: include bidders :D
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
		final EntityManager em = emf.createEntityManager();
		//TODO -> join with auctions, check closureTimeStamp
		List<Bid> l;
		try{
			TypedQuery<Bid> query = em.createQuery("SELECT b FROM Bid b WHERE b.bidder.identity = :id", Bid.class)
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
	
	
	/* Services for avatar */
	@GET
	@Path("{identity}/avatar")
	@Produces("image/*")
	public Document getAvatar(){
		// TODO
	}
	

	@PUT
	@Path("{identity}/avatar")
	@Consumes("image/*")
	public Document setAvatar(){
		// TODO
	}
}
