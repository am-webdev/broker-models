package de.sb.broker.rest;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;
import de.sb.broker.model.Person;

@Path("auctions")
public class AuctionService {

//	static EntityManager em = LifeCycleProvider.brokerManager();
//	static EntityManagerFactory emf = em.getEntityManagerFactory();
//	static CriteriaBuilder cb = em.getCriteriaBuilder();
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getAuctionsXML(){
		final EntityManager em = LifeCycleProvider.brokerManager();
		TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a", Auction.class);
		return query.getResultList();
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public void updateAuctionsXML(){
		
	}
	
	@GET
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	public Auction getAuctionIdentityXML(@PathParam("identity") final long id){
		final EntityManager em = LifeCycleProvider.brokerManager();
		TypedQuery<Auction> query = em
				.createQuery("SELECT a FROM Auction a WHERE p.seller.identity = :id", Auction.class)
				.setParameter("id", id);
		return query.getSingleResult();
	}
}
