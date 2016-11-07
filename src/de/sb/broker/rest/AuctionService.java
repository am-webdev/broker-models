package de.sb.broker.rest;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import de.sb.broker.model.Auction;

@Path("auctions")
public class AuctionService {

	private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("broker");
	private static final EntityManager em = emf.createEntityManager();
	
	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public List<Auction> getAuctionsXML(){
		TypedQuery<Auction> query = em.createQuery("SELECT a FROM Auction a", Auction.class);
		return query.getResultList();
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_XML)
	public void updateAuctionsXML(){
		try{
			em.getTransaction().begin();
			// final Auction = new Auction(Seller); TODO: add seller
			// em.persist(auction)
			// em.getTransaction().commit();
			
		}finally{
			em.close();
		}
	}
	
	@GET
	@Path("{identity}")
	@Produces(MediaType.APPLICATION_XML)
	public Auction getAuctionIdentityXML(@PathParam("identity") final long id){
		TypedQuery<Auction> query = em
				.createQuery("SELECT a FROM Auction a WHERE p.seller.identity = :id", Auction.class)
				.setParameter("id", id);
		return query.getSingleResult();
	}
}
