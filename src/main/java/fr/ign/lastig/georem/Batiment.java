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

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IPopulation;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.geomprim.GM_Point;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.lastig.io.MultipartUtility;

/**
 * 
 * @author Marie-Dominique Van Damme
 */
public class Batiment {
  
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
  private static final String THEME_CODE = "batiment";
  private static final String ATT_US1 = "usage_principal";
  private static final String ATT_US2 = "usage_secondaire";
  private static final String ATT_NBETAGE = "nb_d_etage";
  
  private static final String URL_BATI = "./data/batiment/";

  
  /**
   * 
   */
  public static void postBatimentGeorem(String cleabs, String wkt, String nature, String fonction, String nbetage) {
    try {
      
      nature = fonction;
      fonction = "NULL";

      String attributes = "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "\"=>\"1\","
          + "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "::" + ATT_US1 + "\"=>\"" + nature + "\","
          + "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "::" + ATT_US2 + "\"=>\"" + fonction + "\","
          + "\"" + LANDSENSE_GROUPID + "::" + THEME_CODE + "::" + ATT_NBETAGE + "\"=>\"" + nbetage + "\"";
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
  public static void creationRemonteeBatiment() {
    int cpt = 0;
    try {
      
      IPopulation<IFeature> pointTransition = ShapefileReader.read("./data/batiment/bati_sup250m2_etc.shp");
      Iterator<IFeature> itTransition = pointTransition.getElements().iterator();
      while (itTransition.hasNext()) {
        IFeature transition = (IFeature) itTransition.next();

        String nature = transition.getAttribute("nature").toString();
        
        // Cleabs : il faut croiser ....
        // System.out.println(((GM_Point)transition.getGeom()).getPosition().getX());
        double x = ((GM_Point)transition.getGeom()).getPosition().getX();
        double y = ((GM_Point)transition.getGeom()).getPosition().getY();
        String[] bdtopo = Batiment.getCleabs(x, y);
        String cleabs = bdtopo[0];
        if (cleabs == null) {
          System.out.println(" CLEABS NULL " + x + ", " + y);
        } else {
        
        // Nature : il faut le code
        if (nature != null && nature.length() > 1) {
          if (nature.substring(0, 2).equals("Ar")) {
            nature = "ARENE";
          } else if (nature.substring(0, 2).equals("Ch")) {
            if (nature.length() > 4 && nature.substring(0, 5).equals("Chape")) {
              nature = "CHAPELLE";
            } else {
              nature = "CHATEAU";
            }
          } else if (nature.substring(0, 3).equals("Arc")) {
            nature = "ARC_TRIOMPHE";
          } else if (nature.substring(0, 4).equals("Silo")) {
            nature = "SILO";
          } else if (nature.substring(0, 4).equals("Fort")) {
            nature = "FORT";
          } else if (nature.substring(0, 4).equals("Tour")) {
            nature = "TOUR";
          } else if (nature.substring(0, 5).equals("Serre")) {
            nature = "SERRE";
          } else if (nature.substring(0, 6).equals("Eglise")) {
            nature = "EGLISE";
          } else if (nature.substring(0, 6).equals("Indiff")) {
            nature = "INDIFFERENCIE";
          } else if (nature.substring(0, 6).equals("Indust")) {
            nature = "INDUSTRIEL";
          } else if (nature.substring(0, 6).equals("Tribun")) {
            nature = "TRIBUNE";
          } else if (nature.substring(0, 6).equals("Monume")) {
            nature = "MONUMENT";
          } else {
            System.out.println("nature = " + nature);
          }
          
          
          // Il faut le code de la fonction
          String fonction = transition.getAttribute("fonction").toString();
          
          if (fonction != null && fonction.length() > 1) {
            if (fonction.substring(0, 4).equals("Spor")) {
              fonction = "SPORTIF";
            } else if (fonction.substring(0, 4).equals("Agri")) {
              fonction = "AGRICOLE";
            } else if (fonction.length() > 5 && fonction.substring(0, 6).equals("Indiff")) {
              fonction = "INDIFFERENCIE";
            } else if (fonction.length() > 5 && fonction.substring(0, 6).equals("Indust")) {
              fonction = "INDUSTRIEL";
            } else if (fonction.length() > 5 && fonction.substring(0, 6).equals("Commer")) {
              fonction = "COMMERCIAL";
            } else if (fonction.length() > 3 && fonction.substring(0, 4).equals("Reli")) {
              fonction = "RELIGIEUX";
            } else if (fonction.length() > 3 && fonction.substring(0, 4).equals("Mair")) {
              fonction = "MAIRIE";
            } else if (fonction.length() > 5 && fonction.substring(2, 6).equals("roga")) {
              fonction = "GARE";
            } else if (fonction.length() > 4 && fonction.substring(2, 5).equals("age")) {
              fonction = "PEAGE";
            } else {
              System.out.println(fonction);
            }
          }
          
          
          GM_Point pt = (GM_Point) transition.getGeom();
          String wkt = "POINT(" + pt.getPosition().getX() + " " + pt.getPosition().getY() + ")";
          
          cpt++;
          
          postBatimentGeorem(cleabs, wkt, nature, fonction, "-1");
          //System.out.println("chargé : " + cpt);
          //}
          
          if ((cpt%500) == 0) {
            System.out.println(" ......   " + cpt + " uploadé.");
          //  break;
          }
        
        } else {
          System.out.println("Nature NULL " + nature);
        }
        
        
        }
        
        
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    System.out.println("Nb de batiment uploadé : " + cpt);
  }
  
  
  /**
   * Croisement pour récupérer la cleabs
   * @param x
   * @param y
   * @return
   */
  private static String[] getCleabs(double x, double y) {
    
    String[] attr = new String[2];
    
    String cleabs = null;
    String nature = null;
    
    try {
      
      DirectPosition c = new DirectPosition (y, x);
      GM_Point ps = new GM_Point(c);
      
      CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
      CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2154");
      MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
      Geometry targetGeometry = JTS.transform( JtsGeOxygene.makeJtsGeom(ps), transform);
      
      GM_Point p1 = (GM_Point) JtsGeOxygene.makeGeOxygeneGeom(targetGeometry);
      GM_Point p = new GM_Point(new DirectPosition (p1.coord().get(0).getX(), p1.coord().get(0).getY()));
      // System.out.println("p = " + p.toString());
    
      // On commence par les remarquables
      IPopulation<IFeature> batiTransition = ShapefileReader.read(URL_BATI + "BATI_REMARQUABLE.shp");
      Iterator<IFeature> itTransition = batiTransition.getElements().iterator();
      while (itTransition.hasNext()) {
        IFeature bati = (IFeature) itTransition.next();
        // System.out.println(bati.getGeom().toString());
        if (bati.getGeom().intersects(p)) {
          cleabs = bati.getAttribute("ID").toString();
          nature = bati.getAttribute("NATURE").toString();
          // System.out.println("!!!!! " + cleabs);
        }
        
      }
      
      if (cleabs == null) {
      
        // On continue avec les industriels
        batiTransition = ShapefileReader.read(URL_BATI + "BATI_INDUSTRIEL.shp");
        itTransition = batiTransition.getElements().iterator();
        while (itTransition.hasNext()) {
          IFeature bati = (IFeature) itTransition.next();
          // System.out.println(bati.getGeom().toString());
          if (bati.getGeom().intersects(p)) {
            cleabs = bati.getAttribute("ID").toString();
            nature = bati.getAttribute("NATURE").toString();
            // System.out.println("!!!!! " + cleabs);
          }
        }
        
      }
      
      
      if (cleabs == null) {
        
        // On continue avec les indifferencie
        // On continue avec les industriels
        batiTransition = ShapefileReader.read(URL_BATI+ "BATI_INDIFFERENCIE.shp");
        itTransition = batiTransition.getElements().iterator();
        while (itTransition.hasNext()) {
          IFeature bati = (IFeature) itTransition.next();
          // System.out.println(bati.getGeom().toString());
          if (bati.getGeom().intersects(p)) {
            cleabs = bati.getAttribute("ID").toString();
            nature = "INDIFFERENCIE";
            // System.out.println("!!!!! " + cleabs);
          }
        }
        
      }
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    // 
    attr[0] = cleabs;
    attr[1] = nature;
    return attr;
    
    
  }
  
  
  
  public static void main(String[] args) {
    System.out.println("================");
    Batiment.creationRemonteeBatiment();
    System.out.println("================");
  }

  
}
