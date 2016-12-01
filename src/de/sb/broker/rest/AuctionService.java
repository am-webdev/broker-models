package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Bid;
import de.sb.broker.model.Document;
import de.sb.broker.model.Person;

@Path("auctions")
public class AuctionService {

	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("broker");
	
	/**
	 * Returns the auctions matching the given criteria, with null or missing
	 * parameters identifying omitted criteria
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getAuctions(){
		final EntityManager em = emf.createEntityManager();
		List<Auction> l;
		try{			
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a", Auction.class);
			l =  query.getResultList();
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
			//l = new ArrayList<Auction>();
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
	
	/**
	 * Creates or modifies an auction from the given template data. Note
	 * that an auction may only be modified as long as it is not sealed (i.e. is open and still
	 * without bids).
	 */
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public void createAuction(@Valid Auction tmp){
		final EntityManager em = emf.createEntityManager();
		try{
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
	        em.close();
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
	public void updateAuction(@Valid Auction tmp, @PathParam("identity") @NotNull final Long identity){
		final EntityManager em = emf.createEntityManager();
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
	        em.close();
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
	public List<Auction> getAuctionIdentityXML(@PathParam("identity") @NotNull final long id){
		final EntityManager em = emf.createEntityManager();
		List<Auction> l;
		try{			
			TypedQuery<Auction> query = em
					.createQuery("SELECT a FROM Auction a WHERE a.seller.identity = :id", Auction.class)
					.setParameter("id", id);
			l =  query.getResultList();
		} catch(NoResultException e){
			throw new ClientErrorException(e.getMessage(), 404);
		} catch(Exception e) {
			throw new ClientErrorException(e.getMessage(), 500);
		} finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
}
