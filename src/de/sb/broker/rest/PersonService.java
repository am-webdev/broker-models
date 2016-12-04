package de.sb.broker.rest;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Document;
import de.sb.broker.model.Person;

@Path("people")
public class PersonService {
	
	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("broker");
	private Cache cache = null;
	
	/**
	 * Returns the people matching the given criteria, with null or missing parameters identifying omitted criteria.
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Person> getPeople(
			@QueryParam("lowerVersion") final Integer lowerVersion,
			@QueryParam("upperVersion") final Integer upperVersion,
			@QueryParam("lowerCreationTimeStamp") final Long lowerCreationTimeStamp,
			@QueryParam("upperCreationTimeStamp") final Long upperCreationTimeStamp,
			@QueryParam("alias") final String alias,
			@QueryParam("group") final String group,
			@QueryParam("family") final String family,
			@QueryParam("given") final String given,
			@QueryParam("city") final String city,
			@QueryParam("postCode") final String postCode,
			@QueryParam("street") final String street,
			@QueryParam("email") final String email,
			@QueryParam("phone") final String phone
	){
		final EntityManager em = emf.createEntityManager();
		List<Long> l;
		List<Person> people;
		try{
			TypedQuery<Long> q = em.createQuery("SELECT p.identity FROM Person p WHERE"
					+ "(:lowerVersion IS NULL OR p.version >= :lowerVersion) AND"
					+ "(:upperVersion IS NULL OR p.version <= :upperVersion) AND"
					+ "(:lowerCreationTimeStamp IS NULL OR p.creationTimeStamp >= :lowerCreationTimeStamp) AND"
					+ "(:upperCreationTimeStamp IS NULL OR p.creationTimeStamp <= :upperCreationTimeStamp) AND"
					+ "(:alias IS NULL OR p.alias = :alias) AND"
					+ "(:group IS NULL OR p.group = :group) AND"
					+ "(:family IS NULL OR p.name.family = :family) AND"
					+ "(:given IS NULL OR p.name.given = :given) AND"
					+ "(:city IS NULL OR p.address.city = :city) AND"
					+ "(:postCode IS NULL OR p.address.postCode = :postCode) AND"
					+ "(:street IS NULL OR p.address.street = :street) AND"
					+ "(:email IS NULL OR p.contact.email = :email) AND"
					+ "(:phone IS NULL OR p.contact.phone = :phone)"
					, Long.class);
			q.setParameter("lowerVersion", lowerVersion);
			q.setParameter("upperVersion", upperVersion);
			q.setParameter("lowerCreationTimeStamp", lowerCreationTimeStamp);
			q.setParameter("upperCreationTimeStamp", upperCreationTimeStamp);
			q.setParameter("alias", alias);
			q.setParameter("group", group);
			q.setParameter("family", family);
			q.setParameter("given", given);
			q.setParameter("city", city);
			q.setParameter("postCode", postCode);
			q.setParameter("street", street);
			q.setParameter("email", email);
			q.setParameter("phone", phone);
			
			l =  q.getResultList();
			people = new ArrayList<Person>();
			for (Long id : l) {
				Person p = em.find(Person.class, id);
				if(p != null)
					people.add(p);
			}
			Comparator<Person> comparator = Comparator.comparing(Person::getAlias).thenComparing(Person::getIdentity);
			people.sort(comparator);
			return people;
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
	}
	
	/**
	 * Returns the person matching the given identity.
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getPeopleIdentity(@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
		try{
			Person p = em.find(Person.class, id);
			if(p == null)
				return Response.status(Status.NOT_FOUND).build();
			else
				return Response.ok(p).build();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		
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
		try{
			List<Auction> auctions = new ArrayList<Auction>();
			Person p = em.find(Person.class, id);
			auctions.addAll(p.getAuctions());
			for (Bid b : p.getBids()) {
				auctions.add(b.getAuction());
			}
			Comparator<Auction> comparator = Comparator.comparing(Auction::getClosureTimestamp).thenComparing(Auction::getIdentity);
			auctions.sort(comparator);
			return auctions;
		} finally {
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
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
			List<Bid> bids = new ArrayList<Bid>();
			Person p = em.find(Person.class, id);
			for(Bid b : p.getBids()){
				if(b.getAuction().isClosed())
					bids.add(b);
			}
			return bids;
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
     * @param tmp
     * @param pw
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setPerson(@Valid Person tmp, @HeaderParam("Set-password") final String pw){ 
        final EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            final boolean insertMode = tmp.getIdentity() == 0;
            final Person person;
            if(insertMode)
            	person = new Person();
            else
            	person = em.find(Person.class, tmp.getIdentity());
            person.setAlias(tmp.getAlias());
            person.setGroup(tmp.getGroup());
            person.setName(tmp.getName());
            person.setAddress(tmp.getAddress());
            person.setContact(tmp.getContact());
            person.setPasswordHash(Person.passwordHash(pw));
            if(insertMode)
            	em.persist(person);	
            else
            	em.flush();
            em.getTransaction().commit();
        } finally {
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
	@GET
	@Path("{identity}/avatar")
	@Produces(MediaType.WILDCARD)
	public Response getAvatar(@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
		try{			
			Person p = em.find(Person.class, id);
			Document d = p.getAvatar();
			if(d == null) 
				return Response.status(Status.NOT_FOUND).build();
			else
				return Response.ok(d.getContent(), d.getType()).build();
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
    }

	@PUT
	@Path("{identity}/avatar")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN) // TODO return new id to all Put / Posts 
	public long setAvatar(
			@PathParam("identity") final long personIdentity,
			byte[] content,
			@HeaderParam ("Content-type") final String contentType) throws Exception  {
		
		// Entitiy Manager used several times, but closed after each transition
    	final EntityManager em = emf.createEntityManager();
    	Document uploadedDocument = null;
    	
	    byte[] contentHash = MessageDigest.getInstance("SHA-256").digest(content);
	    
		uploadedDocument = new Document(contentType, content, contentHash);
	    
		/*
		 * find matching avatar based on newly created hash 
		 */
		Long l;
		try {
			TypedQuery<Long> q = em.createQuery("SELECT d.identity FROM Document d WHERE d.hash = :hash", Long.class)
					.setParameter("hash", uploadedDocument.getHash());	// value is stored as "byte[32] --> cannot compare with String
			l = q.getSingleResult();
		} catch(NoResultException e) {
			//TODO catch 
			l = 0l;
		} finally {
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.clear();
		}

		//TODO if hash is found -> find / if hash is not found -> persist
		
		/*
		 * Depending on result length, either a new entry should be stored 
		 * or the MIME type should be updated
		 */
		Document avatar = null;
		try {
			if(l == 0) { // creates new avatar
				em.getTransaction().begin();
				em.persist(uploadedDocument);
				em.getTransaction().commit();
			} else { // Update existing avatar
				em.getTransaction().begin();
				avatar = em.find(Document.class, l);		
				if (uploadedDocument.getType().equals(avatar.getType())) {	// Check of Mime type needs to be updated
					em.getTransaction().begin();
					avatar.setVersion(avatar.getVersion());
					avatar.setType(uploadedDocument.getType());
					em.flush();
				} 
			}
		} finally {
	        if(em.getTransaction().isActive()){
	            System.out.println("Entity Manager Rollback");
	            em.getTransaction().rollback();
	        }
	        em.clear();
		}
		

		/*
		 * Identify person that should be updated
		 * save new avatar to person
		 * commit updated person
		 */
		if(l == 0) {

			try {
				em.getTransaction().begin();
				Person person = em.find(Person.class, personIdentity);
				if (uploadedDocument.getContent().length != 0) {
					person.setAvatar(avatar);
				} else {
					person.setAvatar(null); //Anmerkung: wie soll ein Avatar gelöscht werden? 
				}
				em.flush();
			} finally {
		        if(em.getTransaction().isActive()){
		            System.out.println("Entity Manager Rollback");
		            em.getTransaction().rollback();
		        }
		        em.close();
			}	
		}

		return avatar.getIdentity();
	}
}
