package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.transaction.TransactionalException;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Person;

@Path("auctions")
public class AuctionService {
	
	/**
	 * Returns the auctions matching the given criteria, with null or missing
	 * parameters identifying omitted criteria
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML}) //@Bid.XmlBidderAsEntityFilter
	public List<Auction> getAuctions(
		@NotNull @HeaderParam ("Authorization") String authentication, // TODO remove all @NotNull at authentication
		@QueryParam("lowerVersion") final Integer lowerVersion,
		@QueryParam("upperVersion") final Integer upperVersion,
		@QueryParam("upperCreationTimeStamp") final Long upperCreationTimeStamp,
		@QueryParam("lowerCreationTimeStamp") final Long lowerCreationTimeStamp,
		@QueryParam("title") final String title,
		@QueryParam("upperUnitCount") final Short upperUnitCount,
		@QueryParam("lowerUnitCount") final Short lowerUnitCount,
		@QueryParam("upperAskingPrice") final Long upperAskingPrice,
		@QueryParam("lowerAskingPrice") final Long lowerAskingPrice,
		@QueryParam("upperClosureTimestamp") final Long upperClosureTimestamp,
		@QueryParam("lowerClosureTimestamp") final Long lowerClosureTimestamp,
		@QueryParam("description") final String description,
		@QueryParam("closed") final Boolean closed,
		@QueryParam("offset") final int offset,
		@QueryParam("length") final int length
	){
		final EntityManager em = LifeCycleProvider.brokerManager();

		try{
			List<Auction> auctions;
			List<Long> l;
			TypedQuery<Long> q = em.createQuery("SELECT a.identity FROM Auction a WHERE "
					+ "(:lowerVersion IS NULL OR a.version >= :lowerVersion) AND"
					+ "(:upperVersion IS NULL OR a.version <= :upperVersion) AND"
					+ "(:upperCreationTimeStamp IS NULL OR a.creationTimeStamp >= :upperCreationTimeStamp) AND"
					+ "(:lowerCreationTimeStamp IS NULL OR a.creationTimeStamp <= :lowerCreationTimeStamp) AND"
					+ "(:title IS NULL OR a.title = :title) AND"
					+ "(:upperUnitCount IS NULL OR a.unitCount >= :upperUnitCount) AND"
					+ "(:lowerUnitCount IS NULL OR a.unitCount <= :lowerUnitCount) AND"
					+ "(:upperAskingPrice IS NULL OR a.askingPrice >= :upperAskingPrice) AND"
					+ "(:lowerAskingPrice IS NULL OR a.askingPrice <= :lowerAskingPrice) AND"
					+ "(:upperClosureTimestamp IS NULL OR a.closureTimestamp >= :upperClosureTimestamp) AND"
					+ "(:lowerClosureTimestamp IS NULL OR a.closureTimestamp <= :lowerClosureTimestamp) AND"
					+ "(:description IS NULL OR a.description = :description) AND"
					+ "(:closed IS NULL OR a.closureTimestamp <= :currentDate)"
					, Long.class);
			q.setParameter("lowerVersion", lowerVersion);
			q.setParameter("upperVersion", upperVersion);
			q.setParameter("upperCreationTimeStamp", upperCreationTimeStamp);
			q.setParameter("lowerCreationTimeStamp", lowerCreationTimeStamp);
			q.setParameter("title", title);
			q.setParameter("upperUnitCount", upperUnitCount);
			q.setParameter("lowerUnitCount", lowerUnitCount);
			q.setParameter("upperAskingPrice", upperAskingPrice);
			q.setParameter("lowerAskingPrice", lowerAskingPrice);
			q.setParameter("upperClosureTimestamp", upperClosureTimestamp);
			q.setParameter("lowerClosureTimestamp", lowerClosureTimestamp);
			q.setParameter("description", description);
			q.setParameter("currentDate", System.currentTimeMillis());
			q.setParameter("closed", closed);
			if (offset > 0) {
				q.setFirstResult(offset);
			}
			if (length > 0) {
				q.setMaxResults(length);
			}
			
			l =  q.getResultList();
			auctions = new ArrayList<Auction>();
			for (Long id : l) {
				Auction a = em.find(Auction.class, id);
				if(a != null)
					auctions.add(a);
			}
			Comparator<Auction> comparator = Comparator.comparing(Auction::getClosureTimestamp).thenComparing(Auction::getIdentity);
			auctions.sort(comparator);
			return auctions;
		} finally{ // TODO remove finally if only read method, isActive() is never necessary anymore
			// I'm a teapot!
		}
	}
	
	/**
	 * Creates or modifies an auction from the given template data. Note
	 * that an auction may only be modified as long as it is not sealed (i.e. is open and still
	 * without bids).
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces(MediaType.TEXT_PLAIN)
	public long setAuction(
			@HeaderParam ("Authorization") String authentication,
			@Valid Auction tmp){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		Auction auction = null;
		try{
			final boolean insertMode = tmp.getIdentity() == 0;
			//final Person seller = em.find(Person.class, tmp.getSeller().getIdentity());
			final Person seller = requester;
			if(insertMode)
				auction = new Auction(seller);
			else
				auction = em.find(Auction.class, tmp.getIdentity());
			if(!auction.isSealed()){
				auction.setAskingPrice(tmp.getAskingPrice());
				auction.setClosureTimestamp(tmp.getClosureTimestamp());
				auction.setDescription(tmp.getDescription());
				//auction.setSeller(tmp.getSeller());
				auction.setTitle(tmp.getTitle());
				auction.setUnitCount(tmp.getUnitCount());
				auction.setVersion(tmp.getVersion());
			}
			try {
				if(insertMode)
	            	em.persist(auction);	
	            else
	            	em.flush();
	            em.getTransaction().commit();
				
			} finally { //TODO use this for every put method / committing method
				em.getTransaction().begin();
			}
			
			return auction.getIdentity();
		} catch(ValidationException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} catch(RollbackException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} finally{
			//em.getEntityManagerFactory().getCache().evict(Person.class, auction.getSeller());
			// TODO wirft einen Convert Exception
		}
	}
	
	/**
	 * Returns the auction matching the given identity
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Auction.XmlSellerAsReferenceFilter
	public Auction getAuctionIdentityXML(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		try{
			Auction auction = em.find(Auction.class, id);
			return auction;
		} finally{
			// TODO we can't remove this
		}
	}
	
	/**
	 * Returns the requester's bid for the given auction, or null if none exists.
	 * @param id
	 * @return Bid
	 */
	@GET
	@Path("{identity}/bid")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Bid getBidForAuction(
			@HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		try{
			final Auction a = em.find(Auction.class, id);
			return a.getBid(requester);
		} catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		}
	}
	
	/**
	 * Creates or modifies the requester's bid for the given auction, depending on the requester and the price (in cent) within the
	 * given request body. If the price is zero, then the requester's bid is removed instead.
	 * @param id
	 * @return Bid
	 */
	@POST
	@Path("{identity}/bid")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.TEXT_PLAIN)
	public long setRequestersBid(
			@Valid final long price, 
			@HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id
	){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		Auction auction = null;
		try{
	        auction = em.find(Auction.class, id);
	        Bid bid = auction.getBid(requester);
	        if (bid == null) {
	        	bid = new Bid(auction, requester);
	        } 
	        bid.setPrice(price);
            if(price == 0){
            	em.remove(bid);
            } else if(bid.getIdentity() == 0) {
            	em.persist(bid);	
            } else {
            	em.flush();
        	}
        	try {
           	 	em.getTransaction().commit();
        	} finally {
        		em.getTransaction().begin();
        	}
            return bid.getIdentity();
	    } catch(TransactionalException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} catch(ClientErrorException e) {
			throw new ClientErrorException(403);
		} finally {
			em.getEntityManagerFactory().getCache().evict(Person.class, auction.getSeller());
			for (Bid bid : auction.getBids()) {
				em.getEntityManagerFactory().getCache().evict(Bid.class, bid);
			}
	    }
	}
}