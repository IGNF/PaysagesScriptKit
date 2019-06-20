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
public class ZoneAgricole {
  
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
  private static final String THEME_CODE = "zone_agricole";
  private static final String ATT_CS = "code_cs";
  private static final String ATT_US = "code_us";
  
  
  /**
   * 
   */
  public static void postZoneAgricoleGeorem(String cleabs, String wkt, String cs, String us) {
    try {

      String attributes = "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "\"=>\"1\","
          + "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "::" + ATT_CS + "\"=>\"" + cs + "\","
          + "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "::" + ATT_US + "\"=>\"" + us + "\"";
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
  public static void creationRemonteeZoneAgricole() {
    int cpt = 0;
    try {
      
      IPopulation<IFeature> pointDetection = ShapefileReader.read("./data/usage_agricole/usage_agricole.shp");
      Iterator<IFeature> itDetection = pointDetection.getElements().iterator();
      while (itDetection.hasNext()) {
        IFeature detection = (IFeature) itDetection.next();
       
        String cleabs = detection.getAttribute("ID").toString();
        String cs = detection.getAttribute("CODE_CS").toString();
        String us = detection.getAttribute("CODE_US").toString();
        GM_Point pt = (GM_Point) detection.getGeom();
        String wkt = "POINT(" + pt.getPosition().getX() + " " + pt.getPosition().getY() + ")";
        
        cpt++;
        if (cpt == 35596) {
          postZoneAgricoleGeorem(cleabs, wkt, cs, us);
          System.out.println("chargé : " + cpt + "  (" + cs + "," + us + ")");
        } else {
          System.out.println("déjà chargé : " + cpt + ".");
        }
        
        
        
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    System.out.println("Nb de detection uploadee : " + cpt);
  }

}
