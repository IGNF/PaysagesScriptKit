/**
 * 
 * This software is released under the licence CeCILL
 * 
 * see LICENSE.TXT
 * 
 * see <http://www.cecill.info/ http://www.cecill.info/
 * 
 * 
 * @copyright IGN
 * 
 * 
 */
package fr.ign.lastig.campaign2;

import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.lastig.io.MultipartUtility;

/**
 * 
 * 
 * @author Marie-Dominique Van Damme
 */
public class DetectionChangement {
	
	/** Proxy. */
	private static final boolean useProxy = true;
	  
	/** URL 's service. */
	private static final String urlService = "https://paysages.ign.fr/api/georem/georem_post.xml";

	/** Chaset to encoding. */
	private static final String CHARSET = "UTF-8";
	  
	/** Basic authentification. */
	private static final String ENCODING = Base64.getEncoder().encodeToString(("XXX:XXX").getBytes());
	
	
	/** Group ID pour la campagne "Campagne guidée LandSense". */
	private static final String LANDSENSE_GROUPID = "10";
	private static final String CHANGEMENT_DETECTION_CODE = "svc_detection_chgt";
	private static final String ATT_TC = "type_changement";
	private static final String ATT_CS = "code_cs";
	private static final String ATT_US = "code_us";
	  
	
	/**
	 * 
	 */
	public static void postChangementDetectionGeorem(String cleabs, String wkt, String typeChangement, String cs, String us) {
	    
		try {

			String attributes = "\"" + LANDSENSE_GROUPID + "::" + CHANGEMENT_DETECTION_CODE + "\"=>\"1\","
					+ "\"" + LANDSENSE_GROUPID + "::" + CHANGEMENT_DETECTION_CODE + "::" + ATT_TC + "\"=>\"" + typeChangement + "\","
					+ "\"" + LANDSENSE_GROUPID + "::" + CHANGEMENT_DETECTION_CODE + "::" + ATT_CS + "\"=>\"" + cs + "\","
					+ "\"" + LANDSENSE_GROUPID + "::" + CHANGEMENT_DETECTION_CODE + "::" + ATT_US + "\"=>\"" + us + "\"";
			System.out.println(attributes);
	      
			MultipartUtility multipart = new MultipartUtility(urlService, CHARSET, ENCODING, useProxy);
	      
			multipart.addHeaderField("User-Agent", "CodeJava");
			multipart.addHeaderField("Test-Header", "Header-Value");
	       
			multipart.addFormField("comment", "");
			multipart.addFormField("geometry", wkt);
			multipart.addFormField("cleabs", cleabs);
			multipart.addFormField("group", LANDSENSE_GROUPID);
			multipart.addFormField("attributes", attributes);
			multipart.addFormField("input_device", "UNKNOWN");
	      
			List<String> response = multipart.finish();
			System.out.println("SERVER REPLIED:");
			for (String line : response) {
				System.out.println(line);
			}
	      
	      
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	  
	
	/**
	 * Boucle sur le shapefile
	 */
	public static void creationRemonteeDetection() {
		
		int cpt = 0;
	    
		try {
	      
			IPopulation<IFeature> pointDetection = ShapefileReader.read("./data/geoville/geoville.shp");
			Iterator<IFeature> itDetection = pointDetection.getElements().iterator();
			while (itDetection.hasNext()) {
				
				IFeature detection = (IFeature) itDetection.next();
	       
				String cleabs = detection.getAttribute("CLEABS").toString();
				String cs = detection.getAttribute("CODE_CS").toString();
				String us = detection.getAttribute("CODE_US").toString();
				GM_Point pt = (GM_Point) detection.getGeom();
				String wkt = "POINT(" + pt.getPosition().getX() + " " + pt.getPosition().getY() + ")";
	        
				String changeType = detection.getAttribute("CONSTRTYPE").toString();
	        
				String ct = "AUTRES";
				if (changeType.equals("0")) {
					ct = "RESIDENTIEL";
				} else if (changeType.equals("1")) {
					ct = "INDUSTRIEL";
				} else if (changeType.equals("2")) {
					ct = "INFRASTRUCTURE";
				} else if (changeType.equals("3")) {
					ct = "AUTRES";
				}
				
				
				/*if (us.equals("US4.1.1")) {
					us = "US4";
				} else if (us.equals("US4.1.2")) {
				} else if (us.equals("US4.1.3")) {
				} else if (us.equals("US4.1.4")) {
				}*/
					
	        
				if (!cleabs.trim().equals("")) {
					postChangementDetectionGeorem(cleabs, wkt, ct, cs, us);
					cpt++;
					System.out.println("chargé : " + cpt + "  (" + cs + "," + us + ")");
				}
	        
			}
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    
	    System.out.println("Nb de detection uploadee : " + cpt);
	  }
}
