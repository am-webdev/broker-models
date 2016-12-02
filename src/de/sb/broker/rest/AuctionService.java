package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
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
	
	/**
	 * Returns the auctions matching the given criteria, with null or missing
	 * parameters identifying omitted criteria
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getAuctions(){
		final EntityManager em = LifeCycleProvider.brokerManager();
		List<Auction> l;
		try{			
			em.getTransaction().begin();
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a", Auction.class);
			l =  query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Auction>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
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
		final EntityManager em = LifeCycleProvider.brokerManager();
		try{
			em.getTransaction().begin();
			em.persist(tmp);
			em.getTransaction().commit();
			em.getTransaction().begin();
		}finally{
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
	public void updateAuction(@Valid Auction tmp, @PathParam("identity") final Long identity){
		final EntityManager em = LifeCycleProvider.brokerManager();
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
			em.getTransaction().begin();
		}finally{
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
	public List<Auction> getAuctionIdentityXML(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		List<Auction> l;
		try{
			em.getTransaction().begin();
			TypedQuery<Auction> query = em
					.createQuery("SELECT a FROM Auction a WHERE a.seller.identity = :id", Auction.class)
					.setParameter("id", id);
			l =  query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Auction>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.getTransaction().begin();
		}
		return l;
	}
}
