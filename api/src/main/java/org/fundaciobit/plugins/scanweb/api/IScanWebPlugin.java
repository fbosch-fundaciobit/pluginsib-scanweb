package org.fundaciobit.plugins.scanweb.api;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fundaciobit.plugins.IPlugin;

/**
 * 
 * @author anadal
 * 
 */
public interface IScanWebPlugin extends IPlugin {

  public static final String SCANTYPE_TIFF = "tif";

  public static final String SCANTYPE_JPG = "jpg";

  public static final String SCANTYPE_PNG = "png";

  public static final String SCANTYPE_PDF = "pdf";

  public static final String SCANTYPE_GIF = "gif";
  
  
  public static final String FLAG_NON_SIGNED = "NonSigned";
  
  public static final String FLAG_SIGNED = "Signed";
  
  /** Codi segur de verificació   */
  public static final String FLAG_CSV = "CSV";
  
  /**  Segell de Temps */
  public static final String FLAG_TIMESTAMP = "Timestamp";
  
  /** Pàgina o imatge addicional amb la informació de l'escaneig */  
  // TODO , InfoPage

  public static final String SCANWEB_BASE_PROPERTY = IPLUGIN_BASE_PROPERTIES + "scanweb.";

  public String getName(Locale locale);


  public boolean filter(HttpServletRequest request, ScanWebConfig config);

  /**
   * 
   * @param absolutePluginRequestPath
   * @param relativePluginRequestPath
   * @param request
   * @param config Configuració desitjada d'escaneig (revisar mètodes getSupportedScanTypes()
   *      i getFlagsByScanType(...)
   * @return URL cap a la pàgina inicial del plugin. Si comença per HTTP es absoluta
   *        en cas contrari es relativa
   * @throws Exception
   */
  public String startScanWebTransaction(String absolutePluginRequestPath,
      String relativePluginRequestPath, HttpServletRequest request,
      ScanWebConfig config)  throws Exception;

  public void endScanWebTransaction(long scanWebID, HttpServletRequest request);

  public void requestGET(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  public void requestPOST(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response)
      throws Exception;

  public void cleanScannedFiles(long scanWebID, HttpServletRequest request);

  // JPG, PNG, GIF, TIFF, PDF, ...
  public List<String> getSupportedScanTypes();

  // 
  /**
   * Retorna les configuracions suportades (Signed, NonSigned, CSV, Timestamp, InfoPage, ...)
   * per aquell tipus d'escaneig. El primer de la llista és la configuració per defecte.
   * @param scanType
   * @return
   */
  public List<Set<String>> getSupportedFlagsByScanType(String scanType);

  /**
   *  Modes suportats: SINCRON i/o ASINCRON
   * @return
   */
  public Set<ScanWebMode> getSupportedScanWebModes();

}