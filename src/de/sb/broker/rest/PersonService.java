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
	public Person getPeopleIdentity(@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
		try{

			Person p = em.find(Person.class, id); // TODO Check for null
			return p;
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
			List<Auction> l = new ArrayList<Auction>();
			Person p = em.find(Person.class, id);
			l.addAll(p.getAuctions());
			for (Bid b : p.getBids()) {
				l.add(b.getAuction());
			}
			//TODO sort
			return l;
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
		// TODO see above
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
     * @param tmp
     * @param pw
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void createPerson(@Valid Person tmp, @HeaderParam("Set-password") final String pw){ //TODO rename set
        final EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            final boolean insertMode = tmp.getIdentity() == 0; // if 0 create new
            final Person person;
            if(insertMode) {
            	person = new Person();
            } else {
            	person = em.find(Person.class, tmp.getIdentity());
      
            }
            //TODO set person.X = tmp.X

            // set password hash
            // example hash 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
            // meaning hello
            tmp.setPasswordHash(Person.passwordHash(pw));
            if(insertMode) {
            	em.persist(person);	
            } else {
            	em.flush();
            }
            em.getTransaction().commit();
        } finally {
            if(em.getTransaction().isActive()){
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
	/**
	 * Creates a new person if the given Person template's identity is zero, or
	 * otherwise updates the corresponding person with template data. Optionally, a new
	 * password may be set using the header field â€œSet-passwordâ€�. Returns the affected
	 * person's identity.
     * @param p
     * @param pw
     
    @PUT
	@Path("{identity}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updatePerson(@Valid Person tmp,
    		@HeaderParam("Set-password") final String pw,
    		@PathParam("identity") final Long personIdentity) {
        final EntityManager em = emf.createEntityManager();
        try{
    		em.getTransaction().begin();
    		Person p = em.find(Person.class, personIdentity);
    		if(tmp.getAlias() != null) p.setAlias(tmp.getAlias());
    		if(tmp.getGroup() != null) p.setGroup(tmp.getGroup());
    		if(tmp.getName() != null) p.setName(tmp.getName());
    		if(tmp.getAddress() != null) p.setAddress(tmp.getAddress());
    		if(tmp.getContact() != null) p.setContact(tmp.getContact());
    		if(pw != "") p.setPasswordHash(Person.passwordHash(pw));
    		em.getTransaction().commit();
        }finally{
            if(em.getTransaction().isActive()){
                System.out.println("Entity Manager Rollback");
                em.getTransaction().rollback();
            }   
			RestHelper.update2ndLevelCache(em, tmp);
            em.close();
        }
    }
	*/
    
	/* Services for avatar */
	@GET
	@Path("{identity}/avatar")
	@Produces(MediaType.WILDCARD)
	public Response getAvatar(@PathParam("identity") final long personIdentity) throws Exception {
		// Select from Database
		final EntityManager em = emf.createEntityManager();
		Document d = null;
		Person p = null;
		try{			
			// with CriteriaQuery
			//mit em lösen TODO
			p = em.find(Person.class, personIdentity);
			d = p.getAvatar();
			if(d == null) return Response.noContent().build();
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
	     
		return Response.ok(d.getContent(), d.getType()).build();
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
