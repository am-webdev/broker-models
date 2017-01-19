package de.sb.broker.rest;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

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
	
	public static BufferedImage resizeImage(final Image image, int width, int height) {
        final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        //below three lines are for RenderingHints for better image quality at cost of higher processing time
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();
        return bufferedImage;
    }

}
