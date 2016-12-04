package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.Comparator;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;

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
	public List<Auction> getAuctions(
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
		final EntityManager em = emf.createEntityManager();

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
					//TODO: cant access isClosed
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
		}finally{
			if(em.getTransaction().isActive()) em.getTransaction().rollback();
			em.close();
		}
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
		}finally{
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
	public void updateAuction(@Valid Auction tmp, @PathParam("identity") final Long identity){
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
	 * Returns the auction matching the given identity
	 * @param id
	 * @return
	 */
	@GET
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	public List<Auction> getAuctionIdentityXML(@PathParam("identity") final long id){
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
