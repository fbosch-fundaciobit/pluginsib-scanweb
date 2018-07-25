package org.fundaciobit.plugins.scanweb.iecisa;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.fundaciobit.plugins.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.plugins.scanweb.api.ScanWebConfig;
import org.fundaciobit.plugins.scanweb.api.ScanWebMode;
import org.fundaciobit.plugins.scanweb.api.ScanWebStatus;
import org.fundaciobit.plugins.scanweb.api.ScannedPlainFile;
import org.fundaciobit.plugins.scanweb.api.ScannedDocument;
import org.fundaciobit.plugins.scanweb.api.ScannedSignedFile;
/*import org.fundaciobit.plugins.signature.api.CommonInfoSignature;
import org.fundaciobit.plugins.signature.api.FileInfoSignature;
import org.fundaciobit.plugins.signature.api.ITimeStampGenerator;
import org.fundaciobit.plugins.signature.api.PdfVisibleSignature;
import org.fundaciobit.plugins.signature.api.PolicyInfoSignature;
import org.fundaciobit.plugins.signature.api.SecureVerificationCodeStampInfo;
import org.fundaciobit.plugins.signature.api.SignaturesSet;
import org.fundaciobit.plugins.signature.api.SignaturesTableHeader;
import org.fundaciobit.plugins.signature.api.StatusSignature;
import org.fundaciobit.plugins.signature.api.StatusSignaturesSet;
import org.fundaciobit.plugins.signatureserver.miniappletinserver.MiniAppletInServerSignatureServerPlugin;
import org.fundaciobit.plugins.signatureserver.miniappletinserver.MiniAppletInServerSignatureServerPlugin.InfoCertificate;
*/
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.PublicCertificatePrivateKeyPair;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

/**
 * 
 * @author anadal
 * 
 */
public class IECISAScanWebPlugin extends AbstractScanWebPlugin {

  private static final String PROPERTY_BASE = SCANWEB_BASE_PROPERTY + "iecisa.";

  public static final int HEIGHT = 350;

  /**
	 * 
	 */
  public IECISAScanWebPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public IECISAScanWebPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public IECISAScanWebPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }
  
  
  public boolean isDebug() {
    return "true".equals(getProperty(PROPERTY_BASE + "debug"));
  }
  
  public boolean forceJNLP() {
    return "true".equals(getProperty(PROPERTY_BASE + "forcejnlp"));
  }
  
  /*public boolean forceSign() {
    return "true".equals(getProperty(PROPERTY_BASE + "forcesign"));
  }*/
  
  public boolean closeWindowWhenFinish() {
    return "true".equals(getProperty(PROPERTY_BASE + "closewindowwhenfinish"));
  }
  
  /*public String getKeyStore() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "sign.keystore");
  }
  
  public String getKeyStoreAlias() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "sign.alias");
  }
  
  public String getKeyStorePassword() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "sign.password");
  }
  
  public String getKeyStoreCertPassword() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "sign.certpassword");
  }
  
  public String getAsunto() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "sign.asunto");
  }*/

  @Override
  public String getName(Locale locale) {
    return "Applet/JNLP ScanWeb";
  }


  @Override
  public boolean filter(HttpServletRequest request, ScanWebConfig config) {
    return super.filter(request, config);
  }

  @Override
  public String startScanWebTransaction(String absolutePluginRequestPath,
      String relativePluginRequestPath, HttpServletRequest request, 
      ScanWebConfig config) throws Exception {

    putTransaction(config);
    config.getStatus().setStatus(ScanWebStatus.STATUS_IN_PROGRESS);

    return relativePluginRequestPath + "/" + INDEX;
  }


  protected static final List<String> SUPPORTED_SCAN_TYPES = Collections
      .unmodifiableList(new ArrayList<String>(Arrays.asList(SCANTYPE_PDF)));

  @Override
  public List<String> getSupportedScanTypes() {
    return SUPPORTED_SCAN_TYPES;
  }

  protected static final Set<String> SUPPORTED_FLAG_1 = Collections
      .unmodifiableSet(new HashSet<String>(Arrays.asList(FLAG_NON_SIGNED)));
  
 /* protected static final Set<String> SUPPORTED_FLAG_2 = Collections
      .unmodifiableSet(new HashSet<String>(Arrays.asList(FLAG_SIGNED)));*/

  protected static final List<Set<String>> SUPPORTED_FLAGS = Collections
      .unmodifiableList(new ArrayList<Set<String>>(Arrays.asList(SUPPORTED_FLAG_1)));
  
  /*protected static final List<Set<String>> SUPPORTED_FLAGS_ONLYSIGN = Collections
      .unmodifiableList(new ArrayList<Set<String>>(Arrays.asList(SUPPORTED_FLAG_2)));*/

  @Override
  public List<Set<String>> getSupportedFlagsByScanType(String scanType) {
    if (SCANTYPE_PDF.equals(scanType)) {
      return /*forceSign()? SUPPORTED_FLAGS_ONLYSIGN :*/ SUPPORTED_FLAGS;
    }
    return null;
  }
  
  protected static final Set<ScanWebMode> SUPPORTED_MODES = Collections
      .unmodifiableSet(new HashSet<ScanWebMode>(Arrays.asList(
          ScanWebMode.ASYNCHRONOUS, ScanWebMode.SYNCHRONOUS)));
  
  @Override
  public Set<ScanWebMode> getSupportedScanWebModes() {
    return SUPPORTED_MODES;
  }

  @Override
  public String getResourceBundleName() {
    return "iecisascanweb";
  }

  @Override
  public void requestGET(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
        request, response, true);
  }

  @Override
  public void requestPOST(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
        request, response, false);

  }

  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------
  // ------------------- REQUEST GET-POST
  // ---------------------------------------
  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------

  /**
   * 
   */
  protected void requestGETPOST(String absolutePluginRequestPath,
      String relativePluginRequestPath, long scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response, boolean isGet) {

    if (!absolutePluginRequestPath.endsWith("/")) {
      absolutePluginRequestPath = absolutePluginRequestPath + "/";
    }
    
    if (!relativePluginRequestPath.endsWith("/")) {
      relativePluginRequestPath = relativePluginRequestPath + "/";
    }
    
    ScanWebConfig fullInfo = getTransaction(scanWebID);

    if (fullInfo == null) {
      String titol = (isGet ? "GET" : "POST") + " " + getName(new Locale("ca"))
          + " PETICIO HA CADUCAT";

      requestTimeOutError(absolutePluginRequestPath, relativePluginRequestPath, query,
          String.valueOf(scanWebID), request, response, titol);

    } else {

      Locale languageUI = new Locale(fullInfo.getLanguageUI());

      if (query.startsWith(ISFINISHED_PAGE)) {
         isFinishedRequest(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
             query, request, response, fullInfo, languageUI);
      } else if (query.startsWith(INDEX)) {

        indexPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
            request, response, fullInfo, languageUI);

      } else if (query.startsWith(APPLET) || query.startsWith("img")) {

        retornarRecursLocal(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, languageUI);

      } else if (query.startsWith(JNLP)) {

        jnlpPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
            request, response, languageUI);

      } else if (query.startsWith(UPLOAD_SCAN_FILE_PAGE)) {

        uploadPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
            request, response, fullInfo, languageUI);
      }  else if (query.startsWith(FINALPAGE)) {

        finalPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
            request, response, fullInfo, languageUI);
      } else {

        super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath,
            scanWebID, fullInfo, query, languageUI, request, response, isGet);
      }

    }

  }

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------- INDEX ----------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String INDEX = "index.html";

  protected void indexPage(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {

    boolean debug = isDebug();
    
    String browser=request.getHeader("user-agent");

    
    
    final boolean isIE = (browser != null ) && (browser.toLowerCase().indexOf("msie") != -1 || browser.indexOf("rv:11.0") != -1);
    
    if (debug) {
      log.info(" BROWSER= " + browser);
      log.info(" IS INTERNET EXPLORER = " + isIE);
    }

    PrintWriter out;
    out = generateHeader(request, response, absolutePluginRequestPath,
        relativePluginRequestPath, languageUI);
    
    String tstyle = debug? "border: 2px solid red":"";
    
    out.println("  <table style=\"min-height:200px;width:100%;height:100%;" + tstyle + "\">");
    
    // ----------------  FILA DE INFORMACIO DE FITXERS ESCANEJATS
    
    out.println("  <tr valign=\"middle\">");
    out.println("    <td align=\"center\">");
    //out.println("      <h3 style=\"padding:5px\">" + getTraduccio("llistatescanejats", languageUI) + "</h3>");
    
    out.println("    <table style=\"border: 2px solid black;\">");
    out.println("     <tr><td>");
    out.println("      <div id=\"escanejats\" style=\"width:400px;\">");
    out.println("        <img alt=\"Esperi\" style=\"vertical-align:middle;z-index:200\" src=\"" + absolutePluginRequestPath + WEBRESOURCE + "/img/ajax-loader2.gif" + "\">");
    out.println("        &nbsp;&nbsp;<i>" +  getTraduccio("esperantservidor", languageUI) + "</i>");
    out.println("      </div>");
    out.println("     </td>");
    if (fullInfo.getMode() == ScanWebMode.SYNCHRONOUS) {
      out.println("<td>");
      //out.println("<br/><input type=\"button\" class=\"btn btn-success\" value=\"" + getTraduccio("final", languageUI) + "\" onclick=\"finalScanProcess()\" />");
      out.println("<button class=\"btn btn-success\" onclick=\"finalScanProcess()\">" + getTraduccio("final", languageUI) + "</button>");
      out.println("</td>");
    }
    out.println("     </tr></table>");
    

    out.println("      <br/>");
    //out.println("  <input type=\"button\" class=\"btn btn-primary\" onclick=\"gotoCancel()\" value=\"" + getTraduccio("cancel", locale) + "\">");
    out.println("    </td>");
    out.println("  </tr>");
    out.println("  <tr valign=\"middle\">");
    out.println("    <td align=\"center\">");
    
    
    
    // ------------------  APPLET O BOTO DE CARREGA D'APPLET
    
    String dstyle = debug? "border-style:double double double double;":"";
    out.println("<div style=\"" + dstyle + "\">");
    out.println("<center>");
    
    boolean forceJNLP = forceJNLP();

    if (forceJNLP || !isIE ) { // JNLP

      out.println(
          "<script>\n\n"
        + "  function downloadJNLP() {\n"
        + "     location.href=\"" + relativePluginRequestPath   + JNLP + "\";\n"
        + "     ocultar('botojnlp');\n"
        + "     mostrar('missatgejnlp');\n"
        + "  }\n"
        + "\n\n"       
        + " function mostrar(id) {\n"
        + "    document.getElementById(id).style.display = 'block';\n"
        + "};\n"
        + "\n"
        + " function ocultar(id){\n"
        + "   document.getElementById(id).style.display = 'none';\n"
        + " };\n"
        + "\n"
        + "</script>");

      //+ "    document.write('<br/><br/><input type=\"button\" value=\"" + getTraduccio("pitja", languageUI) + "\" onclick=\"downloadJNLP()\" />');\n"
      out.println("  <div id=\"missatgejnlp\" style=\"display: none;\" >");
      out.println("    <br/><br/><h4> S´està descarregant un arxiu jnlp.");
      out.println("     L´ha d´executar per obrir l´aplicació d´escaneig ... </h4><br/>");
      out.println("  </div>\n");
      
      out.println("  <div id=\"botojnlp\" >");
      out.println("    <input type=\"button\" class=\"btn btn-primary\" value=\"" + getTraduccio("pitja", languageUI) + "\" onclick=\"downloadJNLP();\" /><br/>");
      //+ "     setTimeout(downloadJNLP, 1000);\n" // directament obrim el JNLP
      out.println("  </div>");  
    
    } else {
      // -----------  APPLET --------------------------
      out.println("<script src=\"https://www.java.com/js/deployJava.js\"></script>\n");
      
      out.println(
          "<script>\n\n"
      + "   var attributes = {\n"
      + "    id:'iecisa_scan',\n"
      + "    code:'es.ieci.tecdoc.fwktd.applets.scan.applet.IdocApplet',\n"
      + "    archive:'"
      + absolutePluginRequestPath
      + "applet/plugin-scanweb-iecisascanweb-applet.jar',\n"
      + "    width: " + getWidth() + ",\n"        
      + "    height: " + HEIGHT  + "\n"
      + "   };\n"
      + "   var parameters = {\n"
      + "    servlet:'" + absolutePluginRequestPath + UPLOAD_SCAN_FILE_PAGE + "',\n"
      + "    fileFormName:'" + UPLOAD_SCANNED_FILE_PARAMETER + "'\n"
      + "   } ;\n"
      + "   deployJava.runApplet(attributes, parameters, '1.6');");
      out.println("</script>");
    }

    out.println("</center></div>");
   
        
    
        //+ "    var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;\n"
        //+ "    // Firefox 1.0+\n"
        //+ "    var isFirefox = typeof InstallTrigger !== 'undefined';\n"
        //+ "    // At least Safari 3+: \"[object HTMLElementConstructor]\"\n"
        //+ "    var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;\n"
        //+ "    // Internet Explorer 6-11\n"
        //+ "    var isIE = false || !!document.documentMode;\n"
        //+ "    // Edge 20+\n"
        //+ "    var isEdge = !isIE && !!window.StyleMedia;\n"
        //+ "    // Chrome 1+\n"
        //+ "    var isChrome = !!window.chrome && !!window.chrome.webstore;\n"
        //+ "    // Blink engine detection\n"
        //+ "    var isBlink = (isChrome || isOpera) && !!window.CSS;\n"
        //+ "\n"
        // + "    var home;\n"
        // +
        // "    home = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '"
        // + context + "';\n"
//        + "\n"
//        + "   function escanejarAmbFinestra() {\n"
//        + "    var scan;       \n"
//        + "    scan = document.getElementById('iecisa_scan');\n"
//        + "    var result;\n"
//        + "    result = scan.showInWindow();\n"
//        + "    if (result) {\n"
//        + "      alert(\"" + getTraduccio("error.nofinestra", languageUI)+ "\" + result);\n"
//        + "    } else {\n" 
//        + "      // OK\n"
//        + "    }\n"
//        + "  }\n"


        //+ "\n\n\n"
        // + "   alert('IS CROME? ' + isChrome);"
//        + "  if (!isIE) {\n"
//        + "    document.write('<input type=\"button\" class=\"btn btn-primary\" value=\"" + getTraduccio("pitja", languageUI) + "\" onclick=\"escanejarAmbFinestra();\" /><br/>');\n"
//        + "  }\n"
//        + "\n\n"
//
//        + "  if (!isIE) {\n"
//
//        + "  }\n");

     if ((fullInfo.getMode() == ScanWebMode.SYNCHRONOUS))  {
       out.println("<script>\n\n");
       out.println("  function finalScanProcess() {");
       out.println("    if (document.getElementById(\"escanejats\").innerHTML.indexOf(\"ajax\") !=-1) {");
       out.println("      if (!confirm('" + getTraduccio("noenviats", languageUI) +  "')) {");
       out.println("        return;");
       out.println("      };");
       out.println("    };");
       out.println("    location.href=\"" + relativePluginRequestPath   + FINALPAGE + "\";");
       out.println("  }\n");
       out.println("</script>");
     }
     
    
        
    out.println("  </td></tr>");
    out.println("</table>");
    
    
    
    out.println("<script type=\"text/javascript\">");

    out.println();
    out.println("  var myTimer;");
    out.println("  myTimer = setInterval(function () {closeWhenSign()}, 20000);");
    out.println();
    out.println("  function closeWhenSign() {");
    out.println("    var request;");
    out.println("    if(window.XMLHttpRequest) {");
    out.println("        request = new XMLHttpRequest();");
    out.println("    } else {");
    out.println("        request = new ActiveXObject(\"Microsoft.XMLHTTP\");");
    out.println("    }");
    out.println("    request.open('GET', '" + absolutePluginRequestPath + ISFINISHED_PAGE + "', false);");
    out.println("    request.send();"); 
    out.println();
    out.println("    if ((request.status + '') == '" + HttpServletResponse.SC_OK + "') {");
    out.println("      clearTimeout(myTimer);");
    out.println("      myTimer = setInterval(function () {closeWhenSign()}, 4000);");
    out.println("      document.getElementById(\"escanejats\").innerHTML = 'Documents pujats:' + request.responseText;");
    out.println("    } else if ((request.status + '') == '" + HttpServletResponse.SC_REQUEST_TIMEOUT + "') {"); // 
    out.println("      clearTimeout(myTimer);");
    out.println("      window.location.href = '" + fullInfo.getUrlFinal() + "';");
    out.println("    } else {");
    out.println("      clearTimeout(myTimer);");
    out.println("      myTimer = setInterval(function () {closeWhenSign()}, 4000);");
    out.println("    }");
    out.println("  }");
    out.println();
    out.println();
    out.println("</script>");


    generateFooter(out);

  }

  public int getWidth() {
    return 550;
  }
  
  
  

  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------
  // ------------------------------  IS_FINISHED   ------------------------------
  // ----------------------------------------------------------------------------
  // ----------------------------------------------------------------------------
  
  protected static final String ISFINISHED_PAGE = "isfinished";

  
  protected void isFinishedRequest(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {
    
    
    
    List<ScannedDocument> list = fullInfo.getScannedFiles();
    
    try {
    if (list.size() == 0) {
        response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
    } else {
      //  response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      
      if (list.size() == 1) {
        // "S'ha rebut <b>" +  list.size() + "</b> fitxer"
        response.getWriter().println(getTraduccio("rebut.1.fitxer", languageUI, String.valueOf(list.size())));
      } else {
        // "S'han rebut <b>" +  list.size() + "</b> fitxers"
        response.getWriter().println(
            getTraduccio("rebut.n.fitxers", languageUI, String.valueOf(list.size())));
      }
      response.setStatus(HttpServletResponse.SC_OK);
    }
    
    } catch (IOException e) {
      e.printStackTrace();
      try {
        response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      
      
    }
  }
  


  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ---------------------- RECURSOS LOCALS ----------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String APPLET = "applet/";
  



  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ---------------------- JNLP ----------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String JNLP = "jnlp/";

  protected void jnlpPage(String absolutePluginRequestPath, String relativePluginRequestPath,
      long scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
      Locale languageUI) {

    String appletUrlBase = absolutePluginRequestPath + "applet/";

    log.info(" appletUrlBase = ]" + appletUrlBase + "[ ");

    String appletUrl = appletUrlBase + "plugin-scanweb-iecisascanweb-applet.jar";

    log.info(" appletUrl = ]" + appletUrl + "[ ");

    response.setContentType("application/x-java-jnlp-file");
    response.setHeader("Content-Disposition", "filename=\"ScanWebIECISA.jnlp\"");
    response.setCharacterEncoding("utf-8");

    PrintWriter out;
    try {
      out = response.getWriter();
    } catch (IOException e2) {
      log.error(e2.getMessage(), e2);
      return;
    }

    out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    out.println("<jnlp spec=\"1.0+\" codebase=\"" + appletUrl + "\" >");
    out.println("    <information>");
    out.println("        <title>ScanWeb Applet</title>");
    out.println("        <vendor>IECISA</vendor>");
    out.println("        <homepage href=\"http://www.fundaciobit.org/\" />");
    out.println("        <description>ScanWeb Applet de IECISA</description>");
    // out.println("         <icon href=\"" + absolutePluginRequestPath +
    // "/img/portafib.ico" + "\" />");
    out.println("    </information>");
    out.println("    <security>");
    out.println("        <all-permissions/>");
    out.println("    </security>");
    out.println("    <resources>");
    out.println("        <j2se version=\"1.6+\" java-vm-args=\"-Xmx1024m\" />");
    out.println("        <jar href=\"" + appletUrl + "\" main=\"true\" />");
    out.println("        <property name=\"isJNLP\" value=\"true\"/>");
    out.println("        <property name=\"closeWhenUpload\" value=\"" + closeWindowWhenFinish() + "\"/>");
    out.println("    </resources>");
    out.println("    <applet-desc");
    out.println("      documentBase=\"" + appletUrlBase + "\"");
    out.println("      name=\"ScanWeb Applet de IECISA\"");
    out.println("      main-class=\"es.ieci.tecdoc.fwktd.applets.scan.applet.IdocApplet\"");
    out.println("      width=\"" + getWidth() + " \"");
    out.println("      height=\"" + HEIGHT + "\">");
    out.println();

    // ---------------- GLOBALS ----------------

    out.println("       <param name=\"servlet\" value=\"" + absolutePluginRequestPath
        + UPLOAD_SCAN_FILE_PAGE + "\"/>");
    out.println("       <param name=\"fileFormName\" value=\"" + UPLOAD_SCANNED_FILE_PARAMETER
        + "\"/>");

    out.println("   </applet-desc>");
    out.println("</jnlp>");

    out.flush();

  }
  
  
  
  
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // --------------- FINAL PAGE (SINCRON MODE) -------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String FINALPAGE = "finalPage";


  protected void finalPage(String absolutePluginRequestPath,
      String relativePluginRequestPath, long scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {
    
    log.debug("Entra dins FINAL_PAGE(...");

    try {
      response.sendRedirect(fullInfo.getUrlFinal());
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  
  }
  

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ---------------------- UPLOAD PAGE --------------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String UPLOAD_SCAN_FILE_PAGE = "upload/";

  public static final String UPLOAD_SCANNED_FILE_PARAMETER = "scannedfileparam";

  protected void uploadPage(String absolutePluginRequestPath,
      String relativePluginRequestPath, long scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {

    log.debug("Entra dins uploadPage(...");

    Map<String, FileItem> map = super.readFilesFromRequest(request, response);

    if (map == null) {
      return;
    }

    FileItem fileItem = map.get(UPLOAD_SCANNED_FILE_PARAMETER);
    if (fileItem == null) {
      log.error(" No s'ha rebut cap fitxer amb paràmetre "
            + UPLOAD_SCANNED_FILE_PARAMETER);
      return;
    }

    byte[] data;
    try {
      data = IOUtils.toByteArray(fileItem.getInputStream());
    } catch (IOException e) {
      log.error(" No s'ha pogut llegir del request el fitxer amb paràmetre "
          + UPLOAD_SCANNED_FILE_PARAMETER);
      return;
    }

    String name = fileItem.getName();
    if (name != null) {
      name = FilenameUtils.getName(name);
    }
    String mime = fileItem.getContentType();
    if (mime == null) {
      mime = "application/pdf";
    }
    
    ScannedPlainFile singleScanFile = new ScannedPlainFile(name, mime, data);
    
    ScannedSignedFile scannedSignedFile = null;
    
    /*if (/*forceSign() || fullInfo.getFlags().contains(FLAG_SIGNED)) {
          
      try {
        scannedSignedFile = signFile(fullInfo, languageUI, singleScanFile);
        
        singleScanFile = null;
        
      } catch (Exception e) {

        log.error(" Error firmant document: " + e.getMessage(), e);
        return;
      }
      
    }*/

    
    final Date date = new Date(System.currentTimeMillis());
    
    List<Metadata> metadatas = new ArrayList<Metadata>();
    //metadatas.add(new Metadata("TipoDocumental", "TD99"));
    //metadatas.add(new Metadata("EstadoElaboracion", "EE99"));
    //metadatas.add(new Metadata("Identificador", Calendar.getInstance().get(Calendar.YEAR)
    //    + "_" + fullInfo.getScannedFiles().size() + scanWebID));
    metadatas.add(new Metadata("FechaCaptura", date));
    metadatas.add(new Metadata("VersionNTI", "http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e"));
    
    
   

    ScannedDocument scannedDoc = new ScannedDocument();
    scannedDoc.setMetadatas(metadatas);
    scannedDoc.setScannedSignedFile(scannedSignedFile);
    scannedDoc.setScanDate(date);
    scannedDoc.setScannedPlainFile(singleScanFile);


    fullInfo.getScannedFiles().add(scannedDoc);
  }

  public static final  String username = "scanweb"; // configuracio
  
/*XXX  
  public MiniAppletInServerSignatureServerPlugin plugin = null;
  
  
  
  protected ScannedSignedFile signFile(ScanWebConfig fullInfo, Locale languageUI,
      ScannedPlainFile singleScanFile) throws Exception {
    
    

      
      final String filtreCertificats = "";
      
      
      final String asuntoFirma = getAsunto();
      // TODO es necessari?
      String localizacion = null; // "localizacion";

     
      final  String administrationID = null; // No te sentit en API Firma En Servidor
      PolicyInfoSignature policyInfoSignature = new PolicyInfoSignature();
      policyInfoSignature.setPolicyIdentifier("2.16.724.1.3.1.1.2.1.9");
      policyInfoSignature.setPolicyIdentifierHash("G7roucf600+f03r/o0bAOQ6WAs0=");
      policyInfoSignature.setPolicyIdentifierHashAlgorithm("http://www.w3.org/2000/09/xmldsig#sha1");
      policyInfoSignature.setPolicyUrlDocument("https://sede.060.gob.es/politica_de_firma_anexo_1.pdf");
      
      CommonInfoSignature commonInfoSignature = new CommonInfoSignature(languageUI.getLanguage(),
          filtreCertificats, username, administrationID, policyInfoSignature);

      int signNumber = 1;
      String languageSign = languageUI.getLanguage();
      String signType = FileInfoSignature.SIGN_TYPE_PADES;
      int signMode = FileInfoSignature.SIGN_MODE_IMPLICIT;
      boolean userRequiresTimeStamp = false;
      final String signID = String.valueOf(System.currentTimeMillis());
      
      */
      
      
//      PdfReader reader = new PdfReader(new ByteArrayInputStream(singleScanFile.getData()));
      /*
      File sourcePre = File.createTempFile("ScanWebIECISASourceFile", "pdf");
      FileOutputStream sourceOS = new FileOutputStream(sourcePre);
      //convertirPdfToPdfa(reader, sourceOS);
      FileUtils.writeByteArrayToFile(sourcePre, singleScanFile.getData());
      //** falta alguna cosa !!!!
     //reader.close();
      sourceOS.flush();
      sourceOS.close();
      */
      
      /*
      // 6.- Afegir propietats inicials
      InputStream input3 = new ByteArrayInputStream(singleScanFile.getData()); //output2.toByteArray());
      
      File source = File.createTempFile("DigitalSourceFile", "pdf");
      
      //InputStream input3 = new FileInputStream(sourcePre);
      
      PdfReader reader = new PdfReader(input3);
      FileOutputStream sourceFOS = new FileOutputStream(source);
      PdfStamper stamper3 = new PdfStamper(reader, sourceFOS);
     
      Map<String, String> info = reader.getInfo();
      info.put("IECISA_Scan_Web.version", "2.0.0");
      stamper3.setMoreInfo(info);
      stamper3.close();
      
      input3.close();
      sourceFOS.close();
      reader.close();


      
      //FileUtils.writeByteArrayToFile(source, bytesDocumento);
      String name = singleScanFile.getName();
      
      
      final String signerEmail = null;


      // TODO S'hauria d'obtenir de propietat
      String signAlgorithm = FileInfoSignature.SIGN_ALGORITHM_SHA1;

      int signaturesTableLocation = FileInfoSignature.SIGNATURESTABLELOCATION_WITHOUT;
      final PdfVisibleSignature pdfInfoSignature = null;

      final ITimeStampGenerator timeStampGenerator = null;

      // Valors per defcte
      final SignaturesTableHeader signaturesTableHeader = null;
      final SecureVerificationCodeStampInfo csvStampInfo = null;

      FileInfoSignature fileInfo = new FileInfoSignature(signID, source,
          FileInfoSignature.PDF_MIME_TYPE, name, asuntoFirma, 
          localizacion , signerEmail, signNumber,
          languageSign, signType, signAlgorithm, signMode, signaturesTableLocation,
          signaturesTableHeader, pdfInfoSignature, csvStampInfo, userRequiresTimeStamp,
          timeStampGenerator);

      final String signaturesSetID = String.valueOf(System.currentTimeMillis());
      SignaturesSet signaturesSet = new SignaturesSet(signaturesSetID, commonInfoSignature,
          new FileInfoSignature[] { fileInfo });

      final String timestampUrlBase = null;
      
      
      if (plugin == null) {
        plugin = new MiniAppletInServerSignatureServerPlugin();
        
        String passwordks = getKeyStorePassword();
        
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        File ksFile = new File(getKeyStore());
        ks.load(new FileInputStream(ksFile),passwordks.toCharArray()); 

        String alias = getKeyStoreAlias();
        String passwordCertificado =getKeyStoreCertPassword();
  
        //Obtener la clave privada
        PrivateKey key = (PrivateKey)ks.getKey(alias, passwordCertificado.toCharArray()); 
            
        
        
        PublicCertificatePrivateKeyPair publicCertificatePrivateKeyPair;
        publicCertificatePrivateKeyPair = new PublicCertificatePrivateKeyPair(
            (X509Certificate)ks.getCertificate(alias), key);
        
        
        DigitalInfoCertificate infoCertificate = new DigitalInfoCertificate(ksFile, publicCertificatePrivateKeyPair);
        plugin.putInfoCertificate(username, infoCertificate);
      
      }
      try {
        signaturesSet = plugin.signDocuments(signaturesSet, timestampUrlBase);
      } finally {
        source.delete();
      }
      
      
      StatusSignaturesSet sss = signaturesSet.getStatusSignaturesSet();

      if (sss.getStatus() != StatusSignaturesSet.STATUS_FINAL_OK) {
        System.err.println("Error General MSG = " + sss.getErrorMsg());
        if (sss.getErrorException() != null) {
          sss.getErrorException().printStackTrace();
        }
        throw new Exception(sss.getErrorMsg());
      } else {
        FileInfoSignature fis = signaturesSet.getFileInfoSignatureArray()[0];
        StatusSignature status = fis.getStatusSignature();
        if (status.getStatus() != StatusSignaturesSet.STATUS_FINAL_OK) {
          if (status.getErrorException() != null) {
            status.getErrorException().printStackTrace();
          }
          System.err.println("Error Firma 1. MSG = " + status.getErrorMsg());
          throw new Exception(status.getErrorMsg());
        } else {
          File dest = status.getSignedData();
          
          byte[] output;
          output = FileUtils.readFileToByteArray(dest);

          if (!dest.delete()) {
            dest.deleteOnExit();
          }
          
          return new ScannedSignedFile(name, output, ScannedSignedFile.PADES_SIGNATURE);

        }
      }
    
  }*/
  
  
  /*
  private class DigitalInfoCertificate implements InfoCertificate {
    
    final File f;
    
    final PublicCertificatePrivateKeyPair publicCertificatePrivateKeyPair;

    */
    /**
     * @param f
     * @param publicCertificatePrivateKeyPair
     */
  /*  
  public DigitalInfoCertificate(File f,
        PublicCertificatePrivateKeyPair publicCertificatePrivateKeyPair) {
      super();
      this.f = f;
      this.publicCertificatePrivateKeyPair = publicCertificatePrivateKeyPair;
    }

    public File getKeyStoreFile() {
      return f;
    }

    public PublicCertificatePrivateKeyPair getPublicCertificatePrivateKeyPair(
        InfoCertificate cinfo) throws Exception {

      return this.publicCertificatePrivateKeyPair;
    }
    
  }
  
*/
}