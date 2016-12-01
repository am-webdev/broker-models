package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
	@Auction.XmlSellerAsEntityFilter
	public List<Auction> getAuctions(
			@NotNull @HeaderParam ("Authorization") String authentication
	){
		final EntityManager em = emf.createEntityManager();
		List<Auction> l;
		LifeCycleProvider.authenticate(authentication);
		try{			
			TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a", Auction.class);
			l =  query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Auction>();
		}finally{
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
	public void setAuction(
			@NotNull @HeaderParam ("Authorization") String authentication,
			@Valid @NotNull Auction template
	){
		final EntityManager em = emf.createEntityManager();
		try{
			if(template.getIdentity() == 0){ // creates new auction
				em.getTransaction().begin();
				em.persist(template);
				em.getTransaction().commit();
			}else{
				em.getTransaction().begin();
				Auction toUpdate = em.find(Auction.class, template.getIdentity());
				if(!toUpdate.isClosed() && toUpdate.getBids().size() <= 0){ // update auction
					if(template.getAskingPrice() != 0) {
						toUpdate.setAskingPrice(template.getAskingPrice());
					}
					if(template.getClosureTimestamp() != 0) {
						toUpdate.setClosureTimestamp(template.getClosureTimestamp());
					}
					if(template.getDescription() != null) {
						toUpdate.setDescription(template.getDescription());
					}
					if(template.getSeller() != null) {
						toUpdate.setSeller(template.getSeller());
					}
					if(template.getTitle() != null) {
						toUpdate.setTitle(template.getTitle());
					}
					if(template.getUnitCount() != 0) {
						toUpdate.setUnitCount(template.getUnitCount());
					}
					if(template.getVersion() != 0) {
						toUpdate.setVersion(template.getVersion());
					}
				}
				em.getTransaction().commit();
			}
		}finally{
	        if(em.getTransaction().isActive()){
	            System.out.println("Entity Manager Rollback");
	            em.getTransaction().rollback();
	        }   
	        em.clear();
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
	@Auction.XmlSellerAsReferenceFilter
	public List<Auction> getAuctionIdentityXML(
			@NotNull @HeaderParam ("Authorization") String authentication, 
			@PathParam("identity") final long id
	){
		final EntityManager em = emf.createEntityManager();
		List<Auction> l;
		try{			
			TypedQuery<Auction> query = em
					.createQuery("SELECT a FROM Auction a WHERE a.seller.identity = :id", Auction.class)
					.setParameter("id", id);
			l =  query.getResultList();
		}catch(NoResultException e){
			l = new ArrayList<Auction>();
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
		return l;
	}
}
