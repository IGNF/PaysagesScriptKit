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
package fr.ign.lastig.georem;

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
 * @author Marie-Dominique Van Damme
 */
public class ZoneTransition {
  
  /** Proxy. */
  private static final boolean useProxy = true;
  
  /** URL 's service. */
  private static final String urlService = "https://paysages.ign.fr/api/georem/georem_post.xml";
  
  /** Chaset to encoding. */
  private static final String CHARSET = "UTF-8";
  
  /** Basic authentification. */
  private static final String ENCODING = Base64.getEncoder().encodeToString(("XXXXX:XXXXX").getBytes());  
 
  /** Group ID pour la campagne "Campagne guidée LandSense". */
  private static final String LANDSENSE_GROUPID = "7";
  private static final String ZONE_TRANSITION_CODE = "zone_transition";
  private static final String ATT_CS = "code_cs";
  private static final String ATT_US = "code_us";
  
  /**
   * 
   */
  public static void postZoneTransitionGeorem(String cleabs, String wkt, String cs) {
    try {
     
      String attributes = "\"" + LANDSENSE_GROUPID + "::" + ZONE_TRANSITION_CODE + "\"=>\"1\","
          + "\"" + LANDSENSE_GROUPID + "::" + ZONE_TRANSITION_CODE + "::" + ATT_CS + "\"=>\"" + cs + "\","
          + "\"" + LANDSENSE_GROUPID + "::" + ZONE_TRANSITION_CODE + "::" + ATT_US + "\"=>\"US6.1\"";
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
  public static void creationRemonteeZoneTransition() {
    int cpt = 0;
    try {
      
      IPopulation<IFeature> pointTransition = ShapefileReader.read("./data/zone_transition/Zones_de_transition.shp");
      Iterator<IFeature> itTransition = pointTransition.getElements().iterator();
      while (itTransition.hasNext()) {
        IFeature transition = (IFeature) itTransition.next();
       
        String cleabs = transition.getAttribute("ID").toString();
        String cs = transition.getAttribute("CODE_CS").toString();
        GM_Point pt = (GM_Point) transition.getGeom();
        String wkt = "POINT(" + pt.getPosition().getX() + " " + pt.getPosition().getY() + ")";
        
        postZoneTransitionGeorem(cleabs, wkt, cs);
        cpt++;
        System.out.println("chargé : " + cpt);
        
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    System.out.println("Nb de detection uploadee : " + cpt);
  }

}
