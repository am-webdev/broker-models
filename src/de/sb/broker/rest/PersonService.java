package de.sb.broker.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import javax.validation.constraints.Size;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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
	public void setPerson(Person p, @HeaderParam("Set-password") final String pw){
		final EntityManager em = emf.createEntityManager();
		try{
			em.getTransaction().begin();
			
			p.setPasswordHash(Person.passwordHash(pw));
			Document d = new Document("hey.png", new byte[]{0,0,1,0}, new byte[]{0,0,1,0});
			p.setAvatar(d);
			em.find(Person.class, p.getIdentity());
			em.merge(p);
			em.merge(d);
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
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a LEFT JOIN a.bids b WHERE a.seller.identity = :id OR b.bidder.identity = :id", Auction.class) //TODO: include bidders :D
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
	
	
	/* Services for avatar */
	@GET
	@Path("{identity}/avatar")
	@Produces("image/*")
	public Response getAvatar(@PathParam("identity")  String id) throws IOException {
		// Select from Database
		final EntityManager em = emf.createEntityManager();
		Document d = null;
		try{
			TypedQuery<Document> query = em
					.createQuery("SELECT d FROM Document d RIGHT JOIN Person p WHERE p.identity = :id", Document.class)
					.setParameter("id", id);
			d = query.getSingleResult();
		}catch(NoResultException e){
			// TODO			
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
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response setAvatar(
			@PathParam("identity")  String id,
			@FormParam("file") ByteArrayInputStream byteArrayInputStream,
			@FormParam("type") String type,
			@FormParam("title") String title,
			@FormParam("hash") byte[] hash) {
		
		final EntityManager em = emf.createEntityManager();
		
		String status = "Upload has been successful";
		Person p;
		
		// data = new ByteArrayInputStream(byteArrayInputStream).read();
		
		Document d = new Document(type, null, hash);
		
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
		
	
		try {
			// TODO Insert into DB
		} catch (Exception e) {
			status = "Upload has failed";
			e.printStackTrace();
		} finally {
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}

		return Response.status(200).entity(status).build();
		// TODO
	}
}
