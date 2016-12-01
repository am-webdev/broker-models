package de.sb.broker.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
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
import de.sb.broker.model.Person.Group;

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
	public void setPerson(
			@NotNull @HeaderParam ("Authorization") String authentication, 
			Person p, @HeaderParam("Set-password") final String pw){
		final EntityManager em = emf.createEntityManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		if(p.getGroup() != Group.ADMIN || (p.getGroup() == Group.ADMIN && requester.getGroup() == Group.ADMIN)) {
			try{
				
				em.getTransaction().begin();
				
				p.setPasswordHash(Person.passwordHash(pw));
				Document d = new Document("application/image-png", new byte[]{0,0,1,0}, new byte[]{0,0,1,0});
				p.setAvatar(d);
				em.find(Person.class, p.getIdentity());
				em.merge(p);
				em.merge(d);
				em.getTransaction().commit();
			}finally{
				if(em.getTransaction().isActive()) em.getTransaction().rollback();
				em.close();
			}
		} else {
			//TODO throw not authorized to set Group to ADMIN if requester not ADMIN
		}
	}
	
	@GET
	@Path("/requester")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Person getRequester(
			@NotNull @HeaderParam ("Authorization") String authentication){
		return LifeCycleProvider.authenticate(authentication);
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
	public List<Bid> getPeopleIdentityBids(
			@NotNull @HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id){
		final EntityManager em = emf.createEntityManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		//TODO -> join with auctions, check closureTimeStamp
		List<Bid> l = new ArrayList<Bid>();
		try{
			TypedQuery<Bid> query;
			if(id == requester.getIdentity()) {
				query = em.createQuery("SELECT b FROM Bid b JOIN b.auction a WHERE b.bidder.identity = :id", Bid.class)
						.setParameter("id", id);
			} else {
				long ts = System.currentTimeMillis();
				query = em.createQuery("SELECT b FROM Bid b JOIN b.auction a WHERE a.closureTimestamp < :ts AND b.bidder.identity = :id", Bid.class)
						.setParameter("id", id)
						.setParameter("ts", ts);
			}
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
	public Response getAvatar(@PathParam("identity")  String id) throws Exception {
		// Select from Database
		final EntityManager em = emf.createEntityManager();
		Document d = null;
		long personIdentity = Long.getLong(id);
		try{
			/*
			 * Working MySQL Statement:
			 * SELECT * FROM Document d 
			 * JOIN Person p ON d.documentIdentity 
			 * WHERE p.avatarReference = d.documentIdentity 
			 * AND p.personIdentity = :id
			 * 
			 * TODO implement as JPQL query
			 */
			TypedQuery<Document> query = em
					.createQuery("SELECT d FROM Document d JOIN Person p WHERE p.identity = :id", Document.class)
					.setParameter("id", personIdentity);
			d = query.getSingleResult();
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		System.out.println(d);
	 
		final ByteArrayInputStream in = new ByteArrayInputStream(d.getContent());
	   
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int data = in.read();
		while (data >= 0) {
			out.write((char) data);
			data = in.read();
		}
		out.flush();
	     
		ResponseBuilder builder = Response.ok(out.toByteArray());
		builder.header("Content-Disposition", "attachment; filename=avatar");
		return builder.build();
    }

	@PUT
	@Path("{identity}/avatar")
	@Consumes(MediaType.WILDCARD)
	public Response setAvatar(
			@PathParam("identity")  String id,
			byte[] fileBytes) throws Exception  {
		
		// Entitiy Manager used several times, but closed after each transition
    	final EntityManager em = emf.createEntityManager();
    	Document uploadedDocument = null;
    	Long personIdentity = Long.parseLong(id);
		
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
					em.merge(avatar);
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
			if (uploadedDocument.getContent().length != 0) {
				person.setAvatar(uploadedDocument);
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
