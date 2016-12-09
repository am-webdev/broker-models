package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getAuctions(
		@NotNull @HeaderParam ("Authorization") String authentication,
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
		@QueryParam("closed") final Boolean closed
	){
		final EntityManager em = LifeCycleProvider.brokerManager();

		try{
			List<Auction> auctions;
			List<Long> l;
			TypedQuery<Long> q = em.createQuery("SELECT a.identity FROM Auction a WHERE"
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
					+ "(:description IS NULL OR a.description = :description)"
					+ "(:closed IS NULL OR a.closureTimestamp < :currentDate)"
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
			q.setParameter("currenteDate", System.currentTimeMillis());
			
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
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
			//l = new ArrayList<Auction>();
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
	}
	
	/**
	 * Creates or modifies an auction from the given template data. Note
	 * that an auction may only be modified as long as it is not sealed (i.e. is open and still
	 * without bids).
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public long setAuction(
			@NotNull @HeaderParam ("Authorization") String authentication,
			@Valid Auction tmp){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		try{
			em.getTransaction().begin();
			final boolean insertMode = tmp.getIdentity() == 0;
			final Auction auction;
			final Person seller = em.find(Person.class, tmp.getSeller().getIdentity());
			if(insertMode)
				auction = new Auction(seller);
			else
				auction = em.find(Auction.class, tmp.getIdentity());
			if(!auction.isSealed()){
				auction.setAskingPrice(tmp.getAskingPrice());
				auction.setClosureTimestamp(tmp.getClosureTimestamp());
				auction.setDescription(tmp.getDescription());
				auction.setSeller(tmp.getSeller());
				auction.setTitle(tmp.getTitle());
				auction.setUnitCount(tmp.getUnitCount());
				auction.setVersion(tmp.getVersion());
			}
			em.getTransaction().commit();
			return auction.getIdentity();
		} catch(ValidationException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} catch(RollbackException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
	        if(em.getTransaction().isActive()){
	            System.out.println("Entity Manager Rollback");
	            em.getTransaction().rollback();
	        }
			RestHelper.update2ndLevelCache(em, tmp);
			em.getTransaction().begin();
		}
	}
	
	/**
	 * Returns the auction matching the given identity
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	@Auction.XmlSellerAsReferenceFilter
	public Auction getAuctionIdentityXML(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		try{
			Auction auction = em.find(Auction.class, id);
			return auction;
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
	 * Returns the requester's bid for the given auction, or null if none exists.
	 * @param id
	 * @return Bid
	 */
	@GET
	@Path("{identity}/bid")
	@Produces(MediaType.APPLICATION_XML)
	public Bid getBidForAuction(
			@NotNull @HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		Bid b;
		try{			
			TypedQuery<Bid> query = em
					.createQuery("SELECT a FROM Auction a RIGHT JOIN a.bids b WHERE a.seller.identity = :id AND b.bidder.identity = ", Bid.class)
					.setParameter("id", id)
					.setParameter("pid", requester.getIdentity());
			b =  query.getSingleResult();
		}catch(NoResultException e){
			b = null;
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
		return b;
	}
	
	/**
	 * Creates or modifies the requester's bid for the given auction, depending on the requester and the price (in cent) within the
	 * given request body. If the price is zero, then the requester's bid is removed instead.
	 * @param id
	 * @return Bid
	 */
	@POST
	@Path("{identity}/bid")
	@Produces(MediaType.APPLICATION_XML)
	public void setRequestersBid(
			@NotNull @HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id,
			@Valid final long price
	){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		Bid b;
		try{			
			//TODO use 2nd level Cache instead of useless bad angry stupid queries
			TypedQuery<Bid> query = em
					.createQuery("SELECT a FROM Auction a RIGHT JOIN a.bids b WHERE a.seller.identity = :id AND b.bidder.identity = ", Bid.class)
					.setParameter("id", id)
					.setParameter("pid", requester.getIdentity());
			b =  query.getSingleResult();
			em.getTransaction().begin();
			if(price == 0) {
				em.remove(b);
			} else {
				b.setPrice(price);
			}
			em.getTransaction().commit();
		}catch(NoResultException e){
			try {
				em.getTransaction().begin();
				Auction a = em.find(Auction.class, id);
				b = new Bid(a, requester);
				b.setPrice(price);
				em.persist(b);
				em.getTransaction().commit();
			} finally {
				if(em.getTransaction().isActive()) em.getTransaction().rollback();
			}
		} finally {
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
	}
}
