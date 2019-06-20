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
public class Carriere {
  
  /** Proxy. */
  private static final boolean useProxy = true;
  
  /** URL 's service. */
  private static final String urlService = "https://paysages.ign.fr/api/georem/georem_post.xml";

  /** Chaset to encoding. */
  private static final String CHARSET = "UTF-8";
  
  /** Basic authentification. */
  private static final String ENCODING = Base64.getEncoder().encodeToString(("XXXXX:XXXXX").getBytes());
  
  /** Shapefile for loadding data. */
  private static final String PATH_CARRIERE = "./data/carriere/carriere.shp";
  
  /** Group ID pour la campagne "Campagne guidée LandSense". */
  private static final String LANDSENSE_GROUPID = "7";
  private static final String CARRIERE_CODE = "carriere";
  private static final String ATT_CS = "code_cs";
  private static final String ATT_US = "code_us";
  
  
  /**
   * 
   * @param cleabs
   * @param wkt
   * @throws Exception 
   */
  public static void postCarriereGeorem(String cleabs, String wkt) throws Exception {
    try {
      
      String attributes = "\"" + LANDSENSE_GROUPID + "::" + CARRIERE_CODE + "\"=>\"1\","
          + "\"" + LANDSENSE_GROUPID + "::" + CARRIERE_CODE + "::" + ATT_CS + "\"=>\"CS1.1.2.1\","
          + "\"" + LANDSENSE_GROUPID + "::" + CARRIERE_CODE + "::" + ATT_US + "\"=>\"US1.3\"";
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
        throw new Exception("Échec de l'import");
    }
  }
  
  
  
  /**
   * Boucle sur le shapefile
   */
  public static void creationRemonteeCarriere() {
    int cpt = 0;
    try {
      
      IPopulation<IFeature> pointCarriere = ShapefileReader.read(PATH_CARRIERE);
      Iterator<IFeature> itCarriere = pointCarriere.getElements().iterator();
      while (itCarriere.hasNext()) {
        IFeature carriere = (IFeature) itCarriere.next();
        
        String cleabs = carriere.getAttribute("ID").toString();
        String cs = carriere.getAttribute("CODE_CS").toString();
        String us = carriere.getAttribute("CODE_US").toString();
        GM_Point pt = (GM_Point) carriere.getGeom();
        String wkt = "POINT(" + pt.getPosition().getX() + " " + pt.getPosition().getY() + ")";
        
        // System.out.println(us);
        
        
        if (cs.equals("CS1.1.2.1") && us.equals("US1.3")) {  
          postCarriereGeorem(cleabs, wkt);
          cpt++;
          System.out.println("chargé : " + cpt);
          
//          if (cpt >= 1) {
//            break;
//          }
          
        } else {
          System.out.println("!!!! Erreur : " + cleabs);
        }
        
        
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    System.out.println("Nb de carriere uploade : " + cpt);
    
  }
  
  
//  public static void main(String[] args) {
//    System.out.println("================");
//    creationRemonteeCarriere();
//    System.out.println("================");
//  }

}
