package de.sb.broker.rest;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
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
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getAuctions(
		@NotNull @HeaderParam ("Authorization") String authentication,
		@QueryParam("closed") final boolean isClosed){
		final EntityManager em = LifeCycleProvider.brokerManager();
		List<Auction> l;
		try{
			em.getTransaction().begin();
			String queryString = "SELECT a FROM Auction a";
			if (isClosed) {
				queryString += " WHERE a.closureTimestamp < "+ System.currentTimeMillis();
				// Can we do something like: queryString = "SELECT a FROM Auction a WHERE a.isClosed = true";
			} 
			TypedQuery<Auction> query = em.createQuery(queryString, Auction.class);
			l =  query.getResultList();
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
			//l = new ArrayList<Auction>();
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
		return l;
	}

	public void createAuction(
		@NotNull @HeaderParam ("Authorization") String authentication,
		@Valid Auction tmp){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		try{
			tmp.setSeller(requester);
			em.getTransaction().begin();
			em.persist(tmp);
			em.getTransaction().commit();
		} catch(ValidationException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} catch(RollbackException e) {
			throw new ClientErrorException(e.getMessage(), 409);
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
	        if(em.getTransaction().isActive()){
	            System.out.println("Entity Manager Rollback");
	            em.getTransaction().rollback();
	        }   
			em.getTransaction().begin();
			RestHelper.update2ndLevelCache(em, tmp);
		}
	}
	
	/**
	 * Creates or modifies an auction from the given template data. Note
	 * that an auction may only be modified as long as it is not sealed (i.e. is open and still
	 * without bids).
	 */
	@PUT
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	public void updateAuction(
		@NotNull @HeaderParam ("Authorization") String authentication,
		@Valid Auction tmp,
		@PathParam("identity") final long identity){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		try{
			em.getTransaction().begin();
			Auction a = em.find(Auction.class, identity);
			if(!a.isClosed() && a.getBids().size() <= 0){ // update auction
				if(tmp.getAskingPrice() != 0) a.setAskingPrice(tmp.getAskingPrice());
				if(tmp.getClosureTimestamp() != 0) a.setClosureTimestamp(tmp.getClosureTimestamp());
				if(tmp.getDescription() != null) a.setDescription(tmp.getDescription());
				if(tmp.getSeller() != null) a.setSeller(tmp.getSeller());
				if(tmp.getTitle() != null) a.setTitle(tmp.getTitle());
				if(tmp.getUnitCount() != 0)a.setUnitCount(tmp.getUnitCount());
				if(tmp.getVersion() != 0)a.setVersion(tmp.getVersion());
			}
			em.getTransaction().commit();
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
	public List<Auction> getAuctionIdentityXML(
			@NotNull @HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		Person requester = LifeCycleProvider.authenticate(authentication);
		List<Auction> l;
		try{
			em.getTransaction().begin();
			TypedQuery<Auction> query = em
					.createQuery("SELECT a FROM Auction a WHERE a.identity = :id", Auction.class)
					.setParameter("id", id);
			l =  query.getResultList();
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
		return l;
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
