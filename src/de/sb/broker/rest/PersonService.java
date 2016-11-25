package de.sb.broker.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
     * @param tmp
     * @param pw
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void createPerson(@Valid Person tmp, @HeaderParam("Set-password") final String pw){
        final EntityManager em = emf.createEntityManager();
        System.out.println(tmp);
        try{
            em.getTransaction().begin();
            
            // set password hash
            // example hash 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
            // meaning hello
            tmp.setPasswordHash(Person.passwordHash(pw));
            
            // set default avatar - obsolete, field can be NULL
            // p.setAvatar(new Document("application/image-png", new byte[]{}, new byte[]{}));
            em.persist(tmp);
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
    
	/**
	 * Creates a new person if the given Person template's identity is zero, or
	 * otherwise updates the corresponding person with template data. Optionally, a new
	 * password may be set using the header field â€œSet-passwordâ€�. Returns the affected
	 * person's identity.
     * @param p
     * @param pw
     */
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
	
	/* Services for avatar */
	@GET
	@Path("{identity}/avatar")
	@Produces(MediaType.WILDCARD)
	public Response getAvatar(@PathParam("identity") final Long personIdentity) throws Exception {
		// Select from Database
		final EntityManager em = emf.createEntityManager();
		Document d = null;
		Person p = null;
		try{			
			// with CriteriaQuery
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Person> q = cb.createQuery(Person.class);
			Root<Person> rootPerson = q.from(Person.class);
			q.where(cb.equal(rootPerson.get("identity"), personIdentity));
			
			d = em.createQuery(q).getSingleResult().getAvatar();
			
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
	     
		ResponseBuilder builder = Response.ok(d.getContent());
		builder.header("Content-Type", d.getType());
		builder.header("Content-Disposition", "attachment; filename=avatar");
		return builder.build();
    }

	@PUT
	@Path("{identity}/avatar")
	@Consumes(MediaType.WILDCARD)
	public Response setAvatar(
			@PathParam("identity")  final long personIdentity,
			byte[] fileBytes) throws Exception  {
		
		// Entitiy Manager used several times, but closed after each transition
    	final EntityManager em = emf.createEntityManager();
    	Document uploadedDocument = null;
		
		/*
		 * Read from Array of Bytes to temporary File "outputfile"
		 */
		File outputFile = new File("avatar");
		FileOutputStream outputStream = new FileOutputStream(outputFile);
	    try {
	        outputStream.write(fileBytes);  //write the bytes and your done. 
	    } finally {
			System.out.println("Describe content:\n\tByte-length (Origin): " + fileBytes.length +
					"\tByte-length (Output): " + outputFile.length());
		}
		
		/*
		 * Get MIME Type based on file
		 */		
	    MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		String mimeType = mimeTypesMap.getContentType(outputFile);
		System.out.println("\tMIME-Type: " + mimeType);

		/*
		 * Calculate SHA-256 Hash of input bytes
		 */
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fileInputSteam = new FileInputStream(outputFile.getPath());

        int nread = 0;
        while ((nread = fileInputSteam.read(fileBytes)) != -1) {
          md.update(fileBytes, 0, nread);
        };
        byte[] mdbytes = md.digest();
        StringBuffer hexString = new StringBuffer();
    	for (int i=0;i<mdbytes.length;i++) {
    		hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
    	}
    	String sha256Hash = hexString.toString();
    	System.out.println("\tSHA-265: " + sha256Hash);
    	
		uploadedDocument = new Document(mimeType, fileBytes, mdbytes);
	    
		/*
		 * find matching avatar based on newly created hash 
		 */
		List<Document> l;
		try{
			TypedQuery<Document> q = em.createQuery("SELECT d FROM Document d WHERE d.hash = :hash", Document.class)
					.setParameter("hash", uploadedDocument.getHash());	// value is stored as "byte[32] --> cannot compare with String
			l =  q.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Document>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.clear();
		}
		System.out.println("Matching entries for given hash ("+ sha256Hash +"): " + l.size());
		
		
		/*
		 * Depending on result length, either a new entry should be stored 
		 * or the MIME type should be updated
		 */
	
		try {
			if(l.size() == 0) { // creates new avatar
				em.getTransaction().begin();
				em.persist(uploadedDocument);
				em.getTransaction().commit();
				System.out.println("saved new avatar to db: " + uploadedDocument.toString());
			} else { // Update existing avatar
				if (uploadedDocument.getType().equals(l.get(0).getType())) {	// Check of Mime type needs to be updated
					em.getTransaction().begin();
					Document avatar = em.find(Document.class, l.get(0).getIdentity());
					avatar.setVersion(avatar.getVersion());
					avatar.setType(uploadedDocument.getType());
					// em.merge(avatar);
					em.getTransaction().commit();
					System.out.println("saved updated avatar within db: " + uploadedDocument.toString());
				} else {
					System.out.println("Nothing to do in here");
				}
			}
		}finally{
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
		
		try {
			em.getTransaction().begin();
			Person person = em.find(Person.class, personIdentity);
			Document doc = em.find(Document.class, l.get(0).getIdentity());
			if (uploadedDocument.getContent().length != 0) {
				person.setAvatar(doc);
			} else {
				person.setAvatar(new Document("", new byte[32], new byte[32]));
				System.out.println("clear avatar of person");
			}
			em.merge(person);
			em.getTransaction().commit();
		} finally {
	        if(em.getTransaction().isActive()){
	            System.out.println("Entity Manager Rollback");
	            em.getTransaction().rollback();
	        }
	        em.close();
		}

		return null;
	}
}
