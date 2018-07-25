package es.limit.plugins.scanweb.dynamicwebtwain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fundaciobit.plugins.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.plugins.scanweb.api.IScanWebPlugin;
import org.fundaciobit.plugins.scanweb.api.ScanWebConfig;
import org.fundaciobit.plugins.scanweb.api.ScanWebMode;
import org.fundaciobit.plugins.scanweb.api.ScanWebStatus;
import org.fundaciobit.plugins.scanweb.api.ScannedDocument;
import org.fundaciobit.plugins.scanweb.api.ScannedPlainFile;
import org.fundaciobit.plugins.scanweb.api.ScannedSignedFile;
/*XXX
import org.fundaciobit.plugins.signature.api.CommonInfoSignature;
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
 * @author LIMIT 
 * @author anadal-fundaciobit (Adaptar a API 2.0.0, afegir firma, afegir suport multiples versions)
 * 
 */
public class DynamicWebTwainScanWebPlugin extends AbstractScanWebPlugin implements IScanWebPlugin {

	protected final Logger log = Logger.getLogger(getClass());

	private static final String PROPERTY_BASE = SCANWEB_BASE_PROPERTY + "dynamicwebtwain.";
	//private static Map<String, Properties> missatges = new HashMap<String, Properties>();

	/**
	 * 
	 */
	public DynamicWebTwainScanWebPlugin() {
		super();
	}

  public boolean isDebug() {
    return "true".equals(getProperty(PROPERTY_BASE + "debug"));
  }
	

  public boolean isTrial() throws Exception {
    return "true".equals(getPropertyRequired(PROPERTY_BASE + "trial"));
  }
  
  public String getDWTVersion() {
    String ver = getProperty(PROPERTY_BASE + "version");
    if (ver == null) {
      return "12.2";
    } else {
      return ver;
    }
    
  }
  
  public String getProductKey() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "productkey");
  }

	private String getDynamicWebTwainProperty(String name) {
		return getProperty(PROPERTY_BASE + name);
	}
	
	File resourcesPath = null;
	
  public File getResourcesPath() throws Exception {
    
    if (resourcesPath == null) {
       String resourcesPathStr =  getPropertyRequired(PROPERTY_BASE + "resourcespath");
       
       File tmp = new File(resourcesPathStr);
       
       if (!tmp.exists()) {
         throw new Exception("No existeix la carpeta " + tmp.getAbsolutePath());
       }
      
       if (!tmp.isDirectory()) {
         throw new Exception("La ruta " + tmp.getAbsolutePath() + " no apunta a una carpeta.");
       }
       
       resourcesPath = tmp;
      
    }
    
    
    return resourcesPath;
  }
	

  /*public boolean forceSign() {
    return "true".equals(getProperty(PROPERTY_BASE + "forcesign"));
  }*/
  
 /* public String getKeyStore() throws Exception {
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
	

	/**
	 * @param propertyKeyBase
	 * @param properties
	 */
	public DynamicWebTwainScanWebPlugin(String propertyKeyBase, Properties properties) {
		super(propertyKeyBase, properties);
	}

	/**
	 * @param propertyKeyBase
	 */
	public DynamicWebTwainScanWebPlugin(String propertyKeyBase) {
		super(propertyKeyBase);
	}

	@Override
	public String getName(Locale locale)  {
		return "DynamicWebTwain";
	}


  @Override
  public boolean filter(HttpServletRequest request, ScanWebConfig config) {
    return super.filter(request, config);
  }

  @Override
  public String startScanWebTransaction(String absolutePluginRequestPath,
      String relativePluginRequestPath, HttpServletRequest request, 
      ScanWebConfig config) throws Exception {

    config.setScannedFiles(new ArrayList<ScannedDocument>());
    
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
  
  /*protected static final Set<String> SUPPORTED_FLAG_2 = Collections
      .unmodifiableSet(new HashSet<String>(Arrays.asList(FLAG_SIGNED)));*/

  protected static final List<Set<String>> SUPPORTED_FLAGS = Collections
      .unmodifiableList(new ArrayList<Set<String>>(Arrays.asList(SUPPORTED_FLAG_1)));

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
    return "dynamicwebtwain";
  }
  
  
  public static final String SCANNER_RESOURCES =  "scanner";
  
  
  @Override
  protected void getJavascriptCSS(HttpServletRequest request,
      String absolutePluginRequestPath, String relativePluginRequestPath, PrintWriter out,
      Locale languageUI) {
    
    super.getJavascriptCSS(request, absolutePluginRequestPath, relativePluginRequestPath, out, languageUI);
   
    out.println("<script type=\"text/javascript\" src=\"" + relativePluginRequestPath + SCANNER_RESOURCES + "/" + getDWTVersion() +  "/dynamsoft.webtwain.initiate.js\"></script>");
    out.println("<script type=\"text/javascript\" src=\"" + relativePluginRequestPath + SCANNER_RESOURCES + "/" + getDWTVersion() +  "/dynamsoft.webtwain.config.js\"></script>");
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
  // ------------------- REQUEST GET-POST ---------------------------------------
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
      
      } else if (query.startsWith(SCANNER_RESOURCES)) {

        if (query.endsWith("dynamsoft.webtwain.config.js")) {
          retornarDynamsoftWebtwainConfig(absolutePluginRequestPath, relativePluginRequestPath,
            scanWebID, query, request, response, languageUI);
        } else {
              
          // RECURSOS SCANNER
          retornarRecursDesdeDirectori(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, languageUI);
        }
        
      } else if (query.startsWith(UPLOAD_PAGE)) {

        uploadPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, fullInfo, languageUI);
        
      } else if (query.startsWith(FINALPAGE)) {

        finalPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, fullInfo, languageUI);
      
      } else {

        super.requestGETPOST(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            fullInfo, query, languageUI, request, response, isGet);
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

    PrintWriter out;
    out = generateHeader(request, response, absolutePluginRequestPath,
        relativePluginRequestPath, languageUI);
    
   
    // Carregam els texts en català per si hi ha algun problema al 
    // carregar els fitxers de missatges multiidioma
    String disp = "Dispositiu";
    String color = "Color";
    String res = "Resolució";
    String duplex = "Duplex";
    String clean = "Borra actual";
    String cleanAll = "Borra tot";
    String msgErrorValidacio = "Hi ha errors en el camp del formulari.";
    String upError = "S\\'ha produït un error, i no s\\'ha pogut pujar el document escanejat.";

    disp = getTraduccio("dwt.dispositiu", languageUI);
    color = getTraduccio("dwt.color", languageUI);
    res = getTraduccio("dwt.resolucio", languageUI);
    duplex = getTraduccio("dwt.duplex", languageUI);
    clean = getTraduccio("dwt.borra.actual", languageUI);
    cleanAll = getTraduccio("dwt.borra.tot", languageUI);
    upError = getTraduccio("dwt.error.upload", languageUI);
    msgErrorValidacio = getTraduccio("dwt.error.validacio", languageUI);
    String pujarServidor = getTraduccio("pujarServidor", languageUI);
  

    
    out.println("<script type=\"text/javascript\">");
    
    if ((fullInfo.getMode() == ScanWebMode.SYNCHRONOUS))  { 
      out.println("  function finalScanProcess() {");
      out.println("    if (document.getElementById(\"escanejats\").innerHTML.indexOf(\"ajax\") !=-1) {");
      out.println("      if (!confirm('" + getTraduccio("noenviats", languageUI) +  "')) {");
      out.println("        return;");
      out.println("      };");
      out.println("    };");
      out.println("    location.href=\"" + relativePluginRequestPath   + FINALPAGE + "\";");
      out.println("  }\n");
    }

    out.println();
    out.println("  var myTimer;");
    //out.println("  myTimer = setInterval(function () {closeWhenSign()}, 20000);");
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
    out.println("      if (myTimer != undefined) { clearTimeout(myTimer);}");
    out.println("      myTimer = setInterval(function () {closeWhenSign()}, 4000);");
    out.println("      document.getElementById(\"escanejats\").innerHTML = '" + getTraduccio("docspujats", languageUI) + ":' + request.responseText;");
    out.println("    } else if ((request.status + '') == '" + HttpServletResponse.SC_REQUEST_TIMEOUT + "') {"); // 
    out.println("      if (myTimer != undefined) { clearTimeout(myTimer); }");
    out.println("      window.location.href = '" + fullInfo.getUrlFinal() + "';");
    out.println("    } else {");
    out.println("      if (myTimer != undefined) { clearTimeout(myTimer); }");
    out.println("      myTimer = setInterval(function () {closeWhenSign()}, 4000);");
    out.println("    }");
    out.println("  }");
    out.println();
    out.println();
    out.println("</script>");
        
   out.print(  "<script>");
   out.print(  " Dynamsoft.WebTwainEnv.RegisterEvent('OnWebTwainReady', Dynamsoft_OnReady);\n");
   out.print(  " var DWObject;\n");
   out.print(  " function Dynamsoft_OnReady() {\n"); 
   out.print(  "   DWObject = Dynamsoft.WebTwainEnv.GetWebTwain('dwtcontrolContainer'); // Get the Dynamic Web TWAIN object that is embeded in the div with id 'dwtcontrolContainer'\n"); 
   out.print(  "   if (DWObject) {\n"); 
   out.print(  "     var count = DWObject.SourceCount\n;"); 
   out.print(  "     for (var i = 0; i < count; i++)\n"); 
   out.print(  "       document.getElementById('scanSource').options.add(new Option(DWObject.GetSourceNameItems(i), i));\n");
   out.print(  "     $(\"#scanSource\").trigger(\"chosen:updated\");\n");
   out.print(  "   }\n" ); 
   out.print(  " }\n");
   out.print(  "\n"); 
   out.print(  " function OnSuccess() {\n"); 
   out.print(  "   console.log('successful');\n"); 
   out.print(  " }\n");
   out.print(  "\n");
   out.print(  " function OnFailure(errorCode, errorString) {\n"); 
   out.print(  "   console.log(errorString);\n");
   out.print(  " }\n");
   out.print(  "\n");
   out.print(  " function AcquireImage() {\n");
//   out.print(  "   debugger;\n");
   out.print(  "   if (DWObject) {\n"); 
   out.print(  "     DWObject.SelectSourceByIndex(document.getElementById('scanSource').selectedIndex);\n"); 
   out.print(  "     DWObject.OpenSource();\n"); 
   out.print(  "     DWObject.IfDisableSourceAfterAcquire = true;\n");
   out.print(  "     if (document.getElementById('scanColor').value == 'N'){\n" ); 
   out.print(  "       DWObject.PixelType = EnumDWT_PixelType.TWPT_BW;\n"); 
   out.print(  "     } else if (document.getElementById('scanColor').value == 'G'){\n"); 
   out.print(  "       DWObject.PixelType = EnumDWT_PixelType.TWPT_GRAY;\n" );
   out.print(  "     } else { //if (document.getElementById('scanColor').value == 'C'){\n"); 
   out.print(  "       DWObject.PixelType = EnumDWT_PixelType.TWPT_RGB;\n" );
   out.print(  "     }\n");
   out.print(  "     if (DWObject.Duplex > 0 && document.getElementById('scanDuplex').value == '2'){\n" ); 
   out.print(  "       DWObject.IfDuplexEnabled = true;\n"); 
   out.print(  "     } else {\n"); 
   out.print(  "       DWObject.IfDuplexEnabled = false;\n"); 
   out.print(  "     }\n");
   out.print(  "     DWObject.IfFeederEnabled = false;\n"); 
   out.print(  "     DWObject.IfShowUI = false;\n");
   out.print(  "     DWObject.IfAutoDiscardBlankpages = true;\n");
   out.print(  "     DWObject.Resolution = parseInt(document.getElementById('scanResolution').value);\n" ); 
   out.print(  "     DWObject.AcquireImage();\n"); 
//   out.print(  "     Dynamsoft_OnReady();\n");
//   out.print(  "     alert('Ha sortit de AcquireImage interna.');\n");
   out.print(  "   }\n");
   out.print(  " }\n");
   out.print(  "\n");
   out.print(  " function btnRemoveSelectedImage_onclick() {\n");
   out.print(  "   if (DWObject) {\n");
   out.print(  "     DWObject.RemoveAllSelectedImages();\n");
   out.print(  "   }\n");
   out.print(  " }\n");
   out.print(  "\n");
   out.print(  " function btnRemoveAllImages_onclick() {\n");
   out.print(  "   if (DWObject) {\n");
   out.print(  "     DWObject.RemoveAllImages();\n");
   out.print(  "   }\n");
   out.print(  " }\n");
   out.print(  "\n");
   out.print(  " function ResetScan() {\n" );
   out.print(  "   if (DWObject) {\n");
   out.print(  "     DWObject.RemoveAllImages();\n");
   out.print(  "   }\n");
   out.print(  "   $('#pestanyes a:first').tab('show')\n");
   out.print(  " }\n");
   out.print(  "\n");
   out.print(  " function UploadScan() {\n");
   out.print(  "   if (DWObject) {\n" );
   out.print(  "     if (DWObject.HowManyImagesInBuffer == 0) {\n");
    //   out.print(  "       if ($('#archivo').val() == \"\"){\n");
    //   out.print(  "         alert('No ha adjuntat cap fitxer ni escanejat cap document.')\n");
    //   out.print(  "         return false;\n");
    //   out.print(  "       } else {\n");
   out.print(  "       return true;\n");
    //   out.print(  "       }\n");
   out.print(  "     }\n");
   

   try {
    URL url = new URL(absolutePluginRequestPath);
    out.print(  "     var strHTTPServer = \"" + url.getHost() + "\";\n" );
    boolean isHTTPS = url.getProtocol().toLowerCase().equals("https");
    out.print(  "     DWObject.IfSSL = " + isHTTPS + "; // Set whether SSL is used\n" );
    
    int port = url.getPort();
    if (port == -1) {
      port = isHTTPS?443:80;
    }
    out.print(  "     DWObject.HTTPPort = " + port + ";\n" );
    
  } catch (MalformedURLException e) {
    log.error(" No s'ha pogut extreure el HostName de la URL absoluta: "
        + absolutePluginRequestPath, new Exception());
    out.print(  "     var strHTTPServer = location.hostname;\n" );
    //out.print(  "     DWObject.IfSSL = false; // Set whether SSL is used\n" );
    
    out.print(  "     var isSSL = (window.location.protocol == 'https:');\n" );
    out.print(  "     DWObject.IfSSL = isSSL; // Set whether SSL is used\n" );
    out.print(  "     DWObject.HTTPPort = location.port != '' ? location.port : (isSSL ? 443 : 80);\n" );
    
    out.print(  "     DWObject.HTTPPort = location.port == '' ? 80 : location.port;\n" );
  }
    
//   out.print(  "     var CurrentPathName = unescape(location.pathname);\n" );
//   out.print(  "     var path = CurrentPathName.substring(0, CurrentPathName.lastIndexOf('/'));\n" );
//   out.print(  "     var idAnex = path.substring(path.lastIndexOf('/') + 1);\n" );
    //bufferOutput.append(  "     var CurrentPath = '/" + getDynamicWebTwainProperty("applicationPath", "regweb") + "';\n" );
    //bufferOutput.append(  "     var strActionPage = CurrentPath + '/" + getDynamicWebTwainProperty("guardarScanPath", "anexo/guardarScan") + "/" + scanWebID + "';\n" ); 
    
   out.print(  "     var strActionPage = '" + relativePluginRequestPath + UPLOAD_PAGE + "';\n" );
    
   //out.print(  "     DWObject.IfSSL = false; // Set whether SSL is used\n" );
   
    
    // TODO Extreure host i port de la URL ABSOLUTA !!!! 
    
   
   out.print(  "     var Digital = new Date();\n");
   out.print(  "     var uploadfilename = Math.floor(new Date().getTime() / 1000) // Uses milliseconds according to local time as the file name\n" ); 
   out.print(  "     var result = DWObject.HTTPUploadAllThroughPostAsPDF(strHTTPServer, strActionPage, uploadfilename + '.pdf');\n" );
   out.print(  "     if (!result) {\n");
   out.print(  "       alert('" + upError + "');\n");
   out.print(  "       return false;\n");
   out.print(  "     }\n");
   out.print(  "   }\n");
   out.print(  "   return true;\n");
   out.print(  " }\n");
    
    
    // TODO S'HA DE CANVIAR O BORRAR (TE SENTIT ???)
    String boto =  getDynamicWebTwainProperty("idBotoDesaAnnex"); //, "desaAnnex");
    if (boto != null) {
     out.print(  " $( document ).ready(function() {\n");
     out.print(  "   $('#"+ boto +"')[0].onclick = null;\n");
     out.print(  "   $('#"+ boto +"').click(function() {  \n");
     out.print(  "         pujarServidor();\n");
     out.print(  "   });\n");
     out.print(  " });\n");
    }
    
    
   out.print(  " function pujarServidor() {\n");
    
    if (getDynamicWebTwainProperty("scriptValidacioJS") != null) {
     out.print(  "   if (DWObject) {\n" );
     out.print(  "     if (DWObject.HowManyImagesInBuffer > 0) {\n");      
     out.print(  "       if("+getDynamicWebTwainProperty("scriptValidacioJS")+") {\n");
     out.print(  "         UploadScan();\n");
     out.print(  "       }else{\n");
     out.print(  "         alert('"+msgErrorValidacio+"');\n");
     out.print(  "         return false;\n");
     out.print(  "       }\n");
     out.print(  "     }\n");
     out.print(  "   }\n");
    }else{
     out.print(  "   UploadScan();\n");
    }
    
    out.print(  "    closeWhenSign();\n");  
   out.print(  " };\n");
    
   out.print(  "</script>");
   out.print(  "\n");
    
    
    // Taula que ho engloba tot
    out.println("  <table style=\"min-height:200px;width:100%;height:100%;\">");

    out.println("  <tr valign=\"top\" >");
    out.println("    <td align=\"center\">");
    
    
    out.println("  <table style=\"min-height:200px;\">");
    
    // ----------------  FILA DE INFORMACIO DE FITXERS ESCANEJATS
    
    out.println("  <tr valign=\"top\" >");
    out.println("    <td align=\"center\">");

    out.println("<br/>");
    out.println("    <table style=\"border: 2px solid black;\">");
    out.println("     <tr><td align=\"center\">");
    out.println("      <div id=\"escanejats\" style=\"width:350px;\">");
    
    out.println("        <img alt=\"Esperi\" style=\"vertical-align:middle;z-index:200\" src=\"" + absolutePluginRequestPath + WEBRESOURCE +"/img/ajax-loader2.gif" + "\"><br/>");
      
    out.println("        <i>" +  getTraduccio("esperantservidor", languageUI) + "</i>");
    out.println("      </div>");
    out.println("     </td>");
    if (fullInfo.getMode() == ScanWebMode.SYNCHRONOUS) {
      out.println("</tr><tr><td align=\"center\">");
      out.println("<br/><button class=\"btn btn-success\" onclick=\"finalScanProcess()\">" + getTraduccio("final", languageUI) + "</button>");
      out.println("</td>");
    }
    
    out.println("     </tr></table>");
    
    
    out.println("      <br/>");
    
    
   out.print(  "<div id=\"scanParams\" class=\"col-xs-6\">\n");
   out.print(  " <div id=\"scanSourceGroup\" class=\"form-group col-xs-12\">\n");
   out.print(  "   <div class=\"col-xs-4 pull-left etiqueta_regweb control-label\">\n");
   out.print(  "     <label for=\"scanSource\" style='margin-right:10px;'><span class=\"text-danger\">&bull;</span> " + disp + "</label>\n");
   out.print(  "     </div>\n");
   out.print(  "     <div class=\"col-xs-8\">\n");
   out.print(  "       <select size=\"1\" id=\"scanSource\" class=\"chosen-select\">\n");
   out.print(  "       </select>\n");
   out.print(  "   </div>\n");
   out.print(  " </div>\n");
   out.print(  "\n");
   out.print(  " <div id=\"scanColorGroup\" class=\"form-group col-xs-12\">\n");
   out.print(  "   <div class=\"col-xs-4 pull-left etiqueta_regweb control-label\">\n");
   out.print(  "     <label for=\"scanColor\" style='margin-right:10px;'><span class=\"text-danger\">&bull;</span> " + color + "</label>\n");
   out.print(  "     </div>\n");
   out.print(  "     <div class=\"col-xs-8\">\n");
   out.print(  "       <select size=\"1\" id=\"scanColor\" class=\"chosen-select\">\n");
   out.print(  "       <option value='N'>B/N</option>");
   out.print(  "       <option value='G' selected='selected'>Gris</option>");
   out.print(  "       <option value='C'>Color</option>");
   out.print(  "       </select>\n");
   out.print(  "   </div>\n");
   out.print(  " </div>\n");
   out.print(  "\n");
   out.print(  " <div id=\"scanResolutionGroup\" class=\"form-group col-xs-12\">\n");
   out.print(  "   <div class=\"col-xs-4 pull-left etiqueta_regweb control-label\">\n");
   out.print(  "     <label for=\"scanResolution\" style='margin-right:10px;'><span class=\"text-danger\">&bull;</span> " + res + "</label>\n");
   out.print(  "     </div>\n");
   out.print(  "     <div class=\"col-xs-8\">\n");
   out.print(  "       <select size=\"1\" id=\"scanResolution\" class=\"chosen-select\">\n");
   out.print(  "       <option value='200' selected='selected'>200</option>");
   out.print(  "       <option value='300'>300</option>");
   out.print(  "       <option value='400'>400</option>");
   out.print(  "       <option value='600'>600</option>");
   out.print(  "       </select>\n");
   out.print(  "   </div>\n");
   out.print(  " </div>\n");
   out.print(  "\n");
   out.print(  " <div id=\"scanDuplexGroup\" class=\"form-group col-xs-12\">\n");
   out.print(  "   <div class=\"col-xs-4 pull-left etiqueta_regweb control-label\">\n");
   out.print(  "     <label for=\"scanDuplex\" style='margin-right:10px;'><span class=\"text-danger\">&bull;</span> " + duplex + "</label>\n");
   out.print(  "     </div>\n");
   out.print(  "     <div class=\"col-xs-8\">\n");
   out.print(  "       <select size=\"1\" id=\"scanDuplex\" class=\"chosen-select\">\n");
   out.print(  "       <option value='1' selected='selected'>Una cara</option>");
   out.print(  "       <option value='2'>Doble cara</option>");
   out.print(  "       </select>\n");
   out.print(  "   </div>\n");
   out.print(  " </div>\n");
   out.print(  "\n");

   out.print(  " <div id=\"scanButtonsGroup\" class=\"form-group col-xs-12\">\n");
   out.print(  "   <div class=\"col-xs-4 pull-left etiqueta_regweb control-label\"></div>\n");
   out.print(  "     <div class=\"col-xs-8\">\n");
   out.print(  "<table><tr>\n");
   out.print(  "     <td><button class=\"btn btn-primary\" type=\"button\" value='Scan' onclick='AcquireImage();return false;' >Scan</button></td>\n");
   out.print(  "     <td><button class=\"btn btn-success\" type=\"button\" value='" + clean + "' onclick='pujarServidor();' >" + pujarServidor +"</button></td>\n");
   out.print(  "</tr><tr>\n");    
   out.print(  "     <td><button class=\"btn btn-warning\" type=\"button\" value='" + clean + "' onclick='btnRemoveSelectedImage_onclick();' >" + clean +"</button></td>\n");
   out.print(  "     <td><button class=\"btn btn-danger\" type=\"button\" value='" + cleanAll + "' onclick='btnRemoveAllImages_onclick();' >" + cleanAll + "</button></td>\n");
   out.print(  "</tr></table>\n");
   out.print(  "   </div>\n");
   out.print(  " </div>\n");
   out.print(  "\n");
   out.print(  "</div>");
   out.print(  "\n");

   out.print(" </td><td>\n");
    
   out.print(  "<div id=\"scanContainerGroup\" class=\"col-xs-6\" style=\"margin-bottom: 5px;\">\n");
   out.print(  " <div id='dwtcontrolContainer'></div>");
   out.print(  "</div>");
    
   out.print(" </td></tr></table>\n");
    
    
     // Taula que ho engloba tot i centra el contingut
    out.println("  </td></tr></table>");
    

    generateFooter(out);
    
    out.flush();

  }


  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // --------------- CONFIG /scanner/VER/dynamsoft.webtwain.config.js ------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  // TODO fer cache
  protected void retornarDynamsoftWebtwainConfig(String absolutePluginRequestPath,
      String relativePluginRequestPath, long scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response, Locale languageUI) {
    
    String mime = getMimeType(query);
    query = query.replace('\\', '/');

    try {

      byte[] contingut = getRecursDesdeFitxer(query);
        
        String contingutStr = new String(contingut);
        
        contingutStr = contingutStr.replace("X_PATH_X", relativePluginRequestPath + SCANNER_RESOURCES + "/" + getDWTVersion());
        contingutStr = contingutStr.replace("X_TRIAL_X", String.valueOf(isTrial()));
        contingutStr = contingutStr.replace("X_DEBUG_X", String.valueOf(isDebug()));
        contingutStr = contingutStr.replace("X_PRODUCTKEY_X", getProductKey());

        int pos = query.lastIndexOf('/');
        String resourcename = pos == -1 ? query : query.substring(pos + 1);
        
        Writer out = response.getWriter();
        

        response.setContentType(mime);
        response.setHeader("Content-Disposition", "inline; filename=\"" + resourcename + "\"");
        response.setContentLength(contingut.length);


        out.write(contingutStr);
        out.flush();

        return;
      
    } catch (Exception e) {
      log.error("Error llegint recurs " + query, e);
    }

    // ERROR

    String titol = "No trob el recurs " + query;
    requestNotFoundError(titol, absolutePluginRequestPath, relativePluginRequestPath, query,
        String.valueOf(scanWebID), request, response, languageUI);
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

  public static final String UPLOAD_PAGE = "upload";


  protected void uploadPage(String absolutePluginRequestPath,
      String relativePluginRequestPath, long scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response,
      ScanWebConfig fullInfo, Locale languageUI) {

    log.debug("Entra dins uploadPage(...");

    Map<String, FileItem> map = super.readFilesFromRequest(request, response);

    if (map == null || map.size() == 0) {
      log.error(" S'ha cridat a " + UPLOAD_PAGE + " però no s'ha enviat cap arxiu !!!!");
      return;
    }

    // Recollim la primera entrada
    Entry<String,FileItem> entry = new TreeMap<String, FileItem>(map).firstEntry(); 
    FileItem fileItem = entry.getValue();
    
    final String nomFitxer = entry.getKey();
    log.info("UPLOAD:: Processant fitxer amb nom " + nomFitxer);
    

    byte[] data;
    try {
      data = IOUtils.toByteArray(fileItem.getInputStream());
    } catch (IOException e) {
      log.error(" No s'ha pogut llegir del request el fitxer amb paràmetre "
          + nomFitxer);
      return;
    }

    String name = fileItem.getName();
    if (name != null) {
      name = FilenameUtils.getName(name);
    }
    /*
    String mime = fileItem.getContentType();
    if (mime == null) {
      mime = "application/pdf";
    }
    */
    String mime = "application/pdf";
  
    final Date date = new Date(System.currentTimeMillis());
    
    List<Metadata> metadatas = new ArrayList<Metadata>();
    //metadatas.add(new Metadata("TipoDocumental", "TD99"));
    //metadatas.add(new Metadata("EstadoElaboracion", "EE99"));
    //metadatas.add(new Metadata("Identificador", Calendar.getInstance().get(Calendar.YEAR)
    //    + "_" + fullInfo.getScannedFiles().size() + scanWebID));
    metadatas.add(new Metadata("FechaCaptura", date));
    metadatas.add(new Metadata("VersionNTI", "http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e"));
    

    ScannedPlainFile singleScanFile = new ScannedPlainFile(name, mime, data);
    
    
    ScannedSignedFile scannedSignedFile = null;
    
    if (/*forceSign() ||*/fullInfo.getFlags().contains(FLAG_SIGNED)) {
      
      
      try {
        //scannedSignedFile = signFile(fullInfo, languageUI, singleScanFile);
        
        singleScanFile = null;
        
      } catch (Exception e) {

        log.error(" Error firmant document: " + e.getMessage(), e);
        return;
      }
      
    }
    
    

    ScannedDocument scannedDoc = new ScannedDocument();
    scannedDoc.setMetadatas(metadatas);
    scannedDoc.setScannedSignedFile(scannedSignedFile);
    scannedDoc.setScanDate(date);
    scannedDoc.setScannedPlainFile(singleScanFile);


    fullInfo.getScannedFiles().add(scannedDoc);
    
    log.info("UPLOAD:: FINAL ");
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
    if (isDebug()) {
      log.info(" SCANID[" + fullInfo.getScanWebID()  + "].LIST.SIZE() = " + list.size());
    }
    
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
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      
      
    }
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
      
      
      // 6.- Afegir propietats inicials
      InputStream input3 = new ByteArrayInputStream(singleScanFile.getData()); //output2.toByteArray());
      
      File source = File.createTempFile("DigitalSourceFile", "pdf");
      
      //InputStream input3 = new FileInputStream(sourcePre);
      
      PdfReader reader = new PdfReader(input3);
      FileOutputStream sourceFOS = new FileOutputStream(source);
      PdfStamper stamper3 = new PdfStamper(reader, sourceFOS);
     
      Map<String, String> info = reader.getInfo();
      info.put("DWT_Scan_Web.version", "2.0.0");
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
  
  
  protected void retornarRecursDesdeDirectori(String absolutePluginRequestPath,
      String relativePluginRequestPath, long scanWebID, String query,
      HttpServletRequest request, HttpServletResponse response, Locale languageUI) {
    
    String mime = getMimeType(query);
    query = query.replace('\\', '/');

    query = query.startsWith("/") ? query : ('/' + query);


    
    try {
      
      byte[] contingut = getRecursDesdeFitxer(query);

      int pos = query.lastIndexOf('/');
      String resourcename = pos == -1 ? query : query.substring(pos + 1);
      
      OutputStream out = response.getOutputStream();
      

      response.setContentType(mime);
      response.setHeader("Content-Disposition", "inline; filename=\"" + resourcename + "\"");
      response.setContentLength(contingut.length);


      out.write(contingut);
      out.flush();

        return;
      
    } catch (Exception e) {
      log.error("Error llegint recurs " + query, e);
    }

    // ERROR

    String titol = "No trob el recurs " + query;
    requestNotFoundError(titol, absolutePluginRequestPath, relativePluginRequestPath, query,
        String.valueOf(scanWebID), request, response, languageUI);
  }

  protected byte[] getRecursDesdeFitxer(String query) throws Exception, FileNotFoundException,
      IOException {
    byte[] contingut;
    InputStream input = null;
    
    query = query.startsWith("/") ? query.substring(1) : query;

    try {

      File base = getResourcesPath();
      File f = new File(base, query);

      if (!f.exists()) {
        throw new Exception("S'ha requerit el recurs " + query
            + " però no es troba en la ruta " + f.getAbsolutePath());
      }

      input = new FileInputStream(f);

      contingut = IOUtils.toByteArray(input);
      return contingut;

    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
        }
      }
    }
  }
  
  /*private class DigitalInfoCertificate implements InfoCertificate {
    
    final File f;
    
    final PublicCertificatePrivateKeyPair publicCertificatePrivateKeyPair;
*/
    
    /**
     * @param f
     * @param publicCertificatePrivateKeyPair
     */
   /* public DigitalInfoCertificate(File f,
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
    
  }*/
  

  
}