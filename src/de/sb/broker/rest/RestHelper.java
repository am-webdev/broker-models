package de.sb.broker.rest;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

import de.sb.broker.model.BaseEntity;
import de.sb.broker.model.Document;

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
	
	public static byte[] resizeImage(Document doc, Integer width, Integer height) {

		BufferedImage img = null;
		InputStream in = new ByteArrayInputStream(doc.getContent());
		try {
			img = ImageIO.read(in);
		} catch (IOException e) {
		}
		
		BufferedImage resImg = null;
		double aspectRatio = (double) img.getWidth(null)/(double) img.getHeight(null);

		if (height != null && width != null){
			// re-scale image to fixed values
			resImg = RestHelper.resizeImage(img, width, height);
		} else {
			if (height != null) {
				// auto-scale to fixed Height
				if (height <= 0) {
					throw new ClientErrorException("Illegal rage requested", 400);
				}
				resImg = RestHelper.resizeImage(img, (int) (height/aspectRatio), height);
			}
			if (width != null) {
				// auto-scale to fixed Width 
				if (width <= 0) {
					throw new ClientErrorException("Illegal rage requested", 400);
				}
				resImg = RestHelper.resizeImage(img, width, (int) (width/aspectRatio));
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] imageInByte = new byte[0];
		try {
			ImageIO.write( resImg, "png", baos );
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			
		}	
		return imageInByte;
	}

}
