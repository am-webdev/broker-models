package de.sb.broker.rest;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.transaction.TransactionalException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Document;
import de.sb.broker.model.Person;

@Path("people")
public class PersonService {
	
	public static final long DEFAULT_AVATAR_ID = 1136;
	
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
			@QueryParam("phone") final String phone,
			@QueryParam("offset") final int offset,
			@QueryParam("length") final int length
	){
		final EntityManager em = LifeCycleProvider.brokerManager();
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
			if (offset > 0) {
				q.setFirstResult(offset);
			}
			if (length > 0) {
				q.setMaxResults(length);
			}
					
			
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
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
	}
	
	/**
	 * Returns the person matching the given authentication.
	 * @param authentication
	 * @return
	 */
	@GET
	@Path("/requester")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Person getRequester(
			@HeaderParam ("Authorization") String authentication){
		return LifeCycleProvider.authenticate(authentication);
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
		final EntityManager em = LifeCycleProvider.brokerManager();
		try{
			Person p = em.find(Person.class, id);
			if(p == null) {
				throw new ClientErrorException(404);  // Why to throw this manually but not for auction/{identity} using the same code?
			}
			return p;
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} finally{
			// Why can't this be removed???
		}
		
	}
	
	/**
	 * Returns all auctions associated with the person matching the given identity (as seller or bidder).
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}/auctions")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getPeopleIdentityAuctions(
			@HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id,
			@QueryParam("closed") final Boolean isClosed,
			@QueryParam("seller") final Boolean isSeller ) {
		final EntityManager em = LifeCycleProvider.brokerManager();	
		LifeCycleProvider.authenticate(authentication);
		try{
			List<Auction> auctions = new ArrayList<Auction>();
			Person p = em.find(Person.class, id);
			
			Annotation[] filterAnnotations = new Annotation[]{new Auction.XmlSellerAsEntityFilter.Literal()};

			if (isSeller == null || !isSeller) {
				for (Bid b : p.getBids()) {
					if (isClosed == null) {
						// return open and closed auctions
						auctions.add(b.getAuction());
					} else {
						if (isClosed) {
							filterAnnotations = new Annotation[]{
									new Auction.XmlBidsAsEntityFilter.Literal(),
									new Auction.XmlSellerAsEntityFilter.Literal(),
									new Bid.XmlBidderAsEntityFilter.Literal(),
									new Bid.XmlAuctionAsReferenceFilter.Literal()};

							if (b.getAuction().isClosed()) {
								auctions.add(b.getAuction());
							}
						} else {
							if (!b.getAuction().isClosed()) {
								auctions.add(b.getAuction());
							}
						}
					}
				}
			}
			
			if (isSeller == null || isSeller) {
				for (Auction a : p.getAuctions()) {
					if (isClosed == null) {
						// return open and closed auctions
						auctions.add(a);
					} else {
						if (isClosed) {
							if (isSeller) {
								filterAnnotations = new Annotation[]{
										new Auction.XmlBidsAsEntityFilter.Literal(),
										new Auction.XmlSellerAsReferenceFilter.Literal(),
										new Bid.XmlBidderAsEntityFilter.Literal(),
										new Bid.XmlAuctionAsReferenceFilter.Literal()
										};
							} else {
								filterAnnotations = new Annotation[]{
										new Auction.XmlBidsAsEntityFilter.Literal(),
										new Auction.XmlSellerAsEntityFilter.Literal(),
										new Bid.XmlBidderAsEntityFilter.Literal(),
										new Bid.XmlAuctionAsReferenceFilter.Literal()
										};
							}
							if (a.isClosed()) {
								auctions.add(a);
							}
						} else {
							if (!a.isClosed()) {
								auctions.add(a);
							}
						}
					}
				}	
			}			
			
			Comparator<Auction> comparator = Comparator.comparing(Auction::getClosureTimestamp).thenComparing(Auction::getIdentity);
			auctions.sort(comparator);
			
			GenericEntity<?> wrapper = new GenericEntity<Collection<Auction>>(auctions) {};
			
			return Response.ok().entity(wrapper, filterAnnotations).build();
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} 
	}
	
	/**
	 * Returns all bids for closed auctions associated with the bidder matching the given identity.
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}/bids")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Bid.XmlBidderAsReferenceFilter
	@Bid.XmlAuctionAsReferenceFilter
	public List<Bid> getPeopleIdentityBids(
					@HeaderParam ("Authorization") String authentication, 
					@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		boolean isPerson = false;
		try{
			List<Bid> bids = new ArrayList<Bid>();
			Person p = em.find(Person.class, id);
			isPerson = p.getIdentity() == requester.getIdentity();
			if(p == null) throw new ClientErrorException(404);

			for(Bid b : p.getBids()){
				if (isPerson) {
					bids.add(b);
				}else if(b.getAuction().isClosed())
					bids.add(b);
			}
			
			Comparator<Bid> comparator = Comparator.comparing(Bid::getPrice).thenComparing(Bid::getIdentity);
			bids.sort(comparator);
			return bids;
		} catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} 
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
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces(MediaType.TEXT_PLAIN)
    public long setPerson(@Valid Person tmp, @HeaderParam("Set-password") final String pw){ 
    	final EntityManager em = LifeCycleProvider.brokerManager();
    	Person person = null;
        final boolean insertMode = tmp.getIdentity() == 0;
        if(insertMode) {
        	person = new Person();	
        } else {
        	person = em.find(Person.class, tmp.getIdentity());
        }
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
        
       try {
           em.getTransaction().commit();
       } catch(TransactionalException e) {
    	   throw new ClientErrorException(e.getMessage(), 409);
		} finally {
    	   em.getTransaction().begin();
       }

		for (Auction auction : person.getAuctions()) {
			em.getEntityManagerFactory().getCache().evict(Auction.class, auction);
		}
		for (Bid bid : person.getBids()) {
			em.getEntityManagerFactory().getCache().evict(Bid.class, bid);
		}
        return person.getIdentity();
        
    }
	
	/* Services for avatar */
	@GET
	@Path("{identity}/avatar")
	@Produces(MediaType.WILDCARD)
	public Response getAvatar(
			@PathParam("identity") final long id,
			@QueryParam("w") final Integer requestedWidth,
			@QueryParam("h") final Integer requestedHeight
			){
		final EntityManager em = LifeCycleProvider.brokerManager();
		try{			
			Person p = em.find(Person.class, id);
			if(p == null) throw new ClientErrorException(404);

			Document d = p.getAvatar();
			if(d == null) d = em.find(Document.class, DEFAULT_AVATAR_ID);
			if(d == null) throw new ServerErrorException(500);
				
			if (requestedHeight == null && requestedWidth == null) return Response.ok(d.getContent(), d.getType()).build();

			byte[] resizedImage = RestHelper.resizeImage(d, requestedWidth, requestedHeight);					
			
			return Response.ok(resizedImage, d.getType()).build();
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} 
    }

	@PUT
	@Path("{identity}/avatar")
	@Consumes(MediaType.WILDCARD)
	@Produces(MediaType.TEXT_PLAIN)
	public long setAvatar(
			@PathParam("identity") final long personIdentity,
			byte[] content,
			@HeaderParam ("Content-type") final String contentType) throws Exception  {
		
		// Entitiy Manager used several times, but closed after each transition
		final EntityManager em = LifeCycleProvider.brokerManager();
    	Document uploadedDocument = null;   	
	    byte[] contentHash = MessageDigest.getInstance("SHA-256").digest(content);
	    
		uploadedDocument = new Document(contentType, content, contentHash);
	    
		/*
		 * find matching avatar based on newly created hash 
		 */

		Long l = (long) 0;
		try {
			TypedQuery<Long> q = em.createQuery("SELECT d.identity FROM Document d WHERE d.hash = :hash", Long.class)
					.setParameter("hash", uploadedDocument.getHash());	// value is stored as "byte[32] --> cannot compare with String
			l = q.getSingleResult(); 
		} catch(NoResultException e){
//			throw new ClientErrorException(e.getMessage(), 404);
			
		} catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} 
		boolean	newAvatar = (l == 0);
		/*
		 * Depending on result length, either a new entry should be stored 
		 * or the MIME type should be updated
		 */
		Document avatar = null;
		try {
			if(newAvatar) { // creates new avatar				
				em.persist(uploadedDocument);
		       try {
		           em.getTransaction().commit();
		       } catch(TransactionalException e) {
		    	   throw new ClientErrorException(e.getMessage(), 409);
				} finally {
		    	   em.getTransaction().begin();
		       }
		       avatar = uploadedDocument;
			} else { // Update existing avatar
				avatar = em.find(Document.class, l);		
				if (uploadedDocument.getType().equals(avatar.getType())) {	// Check of Mime type needs to be updated
					avatar.setVersion(avatar.getVersion());
					avatar.setType(uploadedDocument.getType());
					try {
						em.getTransaction().commit();
					} finally {
						em.getTransaction().begin();
					}
					System.out.println("saved updated avatar within db: " + uploadedDocument.toString());
				} else {
					System.out.println("Nothing to do in here");
				}
			}
		} catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} 
		/*
		 * Identify person that should be updated
		 * save new avatar to person
		 * commit updated person
		 */
		Person person;
		try {
			person = em.find(Person.class, personIdentity);
			if (uploadedDocument.getContent().length != 0) {
				person.setAvatar(avatar);
			} else {
				person.setAvatar(null); //TODO Anmerkung: wie soll ein Avatar gelöscht werden? 
			}
			em.flush();

			try {
				em.getTransaction().commit();
			} finally {
				em.getTransaction().begin();
			}
		}  catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} 	
		System.out.println("PUT /avatar person.avatar.getIdentity(): " + person.getAvatar().getIdentity());
		return person.getAvatar().getIdentity();
	}
}