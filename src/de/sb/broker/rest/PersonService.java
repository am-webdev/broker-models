package de.sb.broker.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.Encoded;
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
	
	/**
	 * Returns the people matching the given criteria, with null or missing parameters identifying omitted criteria.
	 * @return
	 */
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
	
	/**
	 * Returns the person matching the given identity.
	 * @param id
	 * @return
	 */
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
	
	/**
	 * Returns all auctions associated with the person matching the given identity (as seller or bidder).
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}/auctions")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getPeopleIdentityAuctions(@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
		List<Auction> l;
		try{
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a LEFT JOIN a.bids b WHERE a.seller.identity = :id OR b.bidder.identity = :id", Auction.class)
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
	
	/**
	 * Returns all bids for closed auctions associated with the bidder matching the given identity.
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}/bids")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Bid> getPeopleIdentityBids(@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
		List<Bid> l = new ArrayList<Bid>();
		try{
			long ts = System.currentTimeMillis();
			TypedQuery<Bid> query = em.createQuery("SELECT b FROM Bid b JOIN b.auction a WHERE a.closureTimestamp < :ts AND b.bidder.identity = :id", Bid.class)
					.setParameter("id", id)
					.setParameter("ts", ts);
			l =  query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Bid>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
	
	/**
	 * Creates a new person if the given Person template's identity is zero, or
	 * otherwise updates the corresponding person with template data. Optionally, a new
	 * password may be set using the header field â€œSet-passwordâ€�. Returns the affected
	 * person's identity.
     * @param p
     * @param pw
     */
	//TODO rename Peson p -> tmp and toUpdate -> p
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setPerson(@Valid Person p, @HeaderParam("Set-password") final String pw){
        final EntityManager em = emf.createEntityManager();
        try{
        	if(p.getIdentity() == 0){ // create new Person
                em.getTransaction().begin();
                
                // set password hash
                // example hash 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
                // meaning hello
                p.setPasswordHash(Person.passwordHash(pw));
                
                // set default avatar
                p.setAvatar(new Document("hey.png", "application/image-png", new byte[]{}, new byte[]{}));
                em.persist(p);
                em.getTransaction().commit();
        	}else{ // update existing Person
        		em.getTransaction().begin();
        		Person toUpdate = em.find(Person.class, p.getIdentity());
        		if(p.getAlias() != null) toUpdate.setAlias(p.getAlias());
        		if(p.getGroup() != null) toUpdate.setGroup(p.getGroup());
        		if(p.getName() != null) toUpdate.setName(p.getName());
        		if(p.getAddress() != null) toUpdate.setAddress(p.getAddress());
        		if(p.getContact() != null) toUpdate.setContact(p.getContact());
        		if(pw != "") toUpdate.setPasswordHash(Person.passwordHash(pw));
        		em.getTransaction().commit();
        	}
        }finally{
            if(em.getTransaction().isActive()){
                System.out.println("Entity Manager Rollback");
                em.getTransaction().rollback();
            }   
            em.clear();
            em.close();
        }
    }
	
	/* Services for avatar */
	@GET
	@Path("{identity}/avatar")
	@Produces("image/*")
	public Response getAvatar(@PathParam("identity")  String id) throws Exception {
		// Select from Database
		final EntityManager em = emf.createEntityManager();
		Document d = null;
		try{
			TypedQuery<Document> query = em
					.createQuery("SELECT d FROM Document d RIGHT JOIN Person p WHERE p.identity = :id", Document.class)
					.setParameter("id", id);
			d = query.getSingleResult();
		}catch(Exception e){
			throw e;
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
	 
		final ByteArrayInputStream in = new ByteArrayInputStream(d.getContent());
	   
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int data = in.read();
		while (data >= 0) {
			out.write((char) data);
			data = in.read();
		}
		out.flush();
	     
		ResponseBuilder builder = Response.ok(out.toByteArray());
		builder.header("Content-Disposition", "attachment; filename=" + d.getName());
		return builder.build();
    }

	@PUT
	@Path("{identity}/avatar")
	@Consumes(MediaType.WILDCARD)
	public Response setAvatar(
			@PathParam("identity")  String id,
			@Encoded byte[] byteArray,
			@Encoded @FormParam("type") String type,
			@Encoded @FormParam("name") String name,
			@Encoded @FormParam("hash") String hash) throws Exception {
		//Headerparam -> mimetype (nur file wird hochgeladen im body) 
		//Hash wird berechnet
		//TODO Document haben keinen namen!
		// -> es gibt keine Form Params
		final EntityManager em = emf.createEntityManager();
		
		String status = "Upload has been successful";
		Person p;
		
		Document d = new Document(name, type, byteArray, hash.getBytes(StandardCharsets.UTF_8));
		
		try{
			TypedQuery<Person> query = em
					.createQuery("SELECT p FROM Person p WHERE p.identity = :id", Person.class)
					.setParameter("id", id);
			p = query.getSingleResult();
		}catch(NoResultException e){
			throw e;
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		
		try{
			em.getTransaction().begin();
			p.setAvatar(d);
			em.find(Person.class, p.getIdentity());
			em.merge(p);
			em.merge(d);
			em.getTransaction().commit();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}

		return Response.status(200).entity(status).build();
	}
}
