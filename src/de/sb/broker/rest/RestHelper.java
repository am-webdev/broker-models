package de.sb.broker.rest;

import javax.persistence.Cache;
import javax.persistence.EntityManager;

import de.sb.broker.model.BaseEntity;

public class RestHelper {
	
	public static Cache update2ndLevelCache(EntityManager em, BaseEntity entity) {

        Long identity = entity.getIdentity();
        if (em.getEntityManagerFactory().getCache().contains(entity.getClass(), identity)) {
        	em.getEntityManagerFactory().getCache().evict(entity.getClass(), entity.getIdentity());
        }         
        return em.getEntityManagerFactory().getCache();
        
	}

}
