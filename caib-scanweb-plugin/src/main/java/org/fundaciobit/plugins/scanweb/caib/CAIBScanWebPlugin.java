package org.fundaciobit.plugins.scanweb.caib;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.scanweb.api.AbstractScanWebPlugin;
import org.fundaciobit.plugins.scanweb.api.ScanWebConfig;
import org.fundaciobit.plugins.scanweb.api.ScanWebMode;
import org.fundaciobit.plugins.scanweb.api.ScanWebStatus;
import org.fundaciobit.plugins.scanweb.api.ScannedDocument;
import org.fundaciobit.plugins.scanweb.api.ScannedSignedFile;
import org.fundaciobit.plugins.utils.Metadata;
import org.fundaciobit.plugins.utils.MetadataType;

import es.caib.digital.ws.api.v1.copiaautentica.CopiaAutenticaWSService;
import es.caib.digital.ws.api.v1.copiaautentica.CopiaAutenticaWSServiceService;
import es.caib.digital.ws.api.v1.copiaautentica.DatosDocumento;
import es.caib.digital.ws.api.v1.copiaautentica.DocumentoElectronico;
import es.caib.digital.ws.api.v1.copiaautentica.EniContenidoFirma;
import es.caib.digital.ws.api.v1.copiaautentica.EniEnumEstadoElaboracion;
import es.caib.digital.ws.api.v1.copiaautentica.EniEnumOrigenCreacion;
import es.caib.digital.ws.api.v1.copiaautentica.EniEnumTipoDocumental;
import es.caib.digital.ws.api.v1.copiaautentica.EniEnumTipoFirma;
import es.caib.digital.ws.api.v1.copiaautentica.EniEstadoElaboracion;
import es.caib.digital.ws.api.v1.copiaautentica.EniFirmaConCertificado;
import es.caib.digital.ws.api.v1.copiaautentica.EniMetadata;
import es.caib.digital.ws.api.v1.copiaautentica.FirmaElectronica;
import es.caib.digital.ws.api.v1.copiaautentica.InformacionDocumento;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosFirmaElectronica;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.LabelMetadatosComplementarios;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.LabelMetadatosComplementarios.Entry;
import es.caib.digital.ws.api.v1.copiaautentica.TipoFirmaOriginal;
import es.caib.digital.ws.api.v1.entidades.Entidad;
import es.caib.digital.ws.api.v1.entidades.EntidadesWSService;
import es.caib.digital.ws.api.v1.entidades.EntidadesWSServiceImplService;
import es.caib.digital.ws.api.v1.entidades.MetaDato;
import es.caib.digital.ws.api.v1.entidades.ValorMetaDato;


/**
 * 
 * @author anadal
 * 
 */
public class CAIBScanWebPlugin extends AbstractScanWebPlugin {
  
  protected static Logger log = Logger.getLogger(CAIBScanWebPlugin.class);

  private static final String PROPERTY_BASE = SCANWEB_BASE_PROPERTY + "caib.";


  /**
	 * 
	 */
  public CAIBScanWebPlugin() {
    super();
  }

  /**
   * @param propertyKeyBase
   * @param properties
   */
  public CAIBScanWebPlugin(String propertyKeyBase, Properties properties) {
    super(propertyKeyBase, properties);
  }

  /**
   * @param propertyKeyBase
   */
  public CAIBScanWebPlugin(String propertyKeyBase) {
    super(propertyKeyBase);
  }

 
  public String getWSUsername() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "ws_username");
  }
  
  public String getWSPassword()throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "ws_password");
  }
  
  
  public String getWSUrlBase() throws Exception {
    return getPropertyRequired(PROPERTY_BASE + "ws_urlbase");
  }
  
  /**
   * Si val null s'utilitzarà l'entitat per defecte
   * @return
   * @throws Exception
   */
  public String getCodiIntegracio() throws Exception {
    return getProperty(PROPERTY_BASE + "codi_integracio");
  }
  
  
  public boolean isDebug() {
    return "true".equals(getProperty(PROPERTY_BASE + "debug"));
  }
  

  @Override
  public String getName(Locale locale) {
    return "CAIB ScanWeb";
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

  protected static final Set<String> SUPPORTED_FLAG = Collections
      .unmodifiableSet(new HashSet<String>(Arrays.asList(FLAG_SIGNED, FLAG_CSV)));

  protected static final List<Set<String>> SUPPORTED_FLAGS = Collections
      .unmodifiableList(new ArrayList<Set<String>>(Arrays.asList(SUPPORTED_FLAG)));

  @Override
  public List<Set<String>> getSupportedFlagsByScanType(String scanType) {
    if (SCANTYPE_PDF.equals(scanType)) {
      return SUPPORTED_FLAGS;
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
    return "caibscanweb";
  }
  
  
  @Override
  protected void getJavascriptCSS(HttpServletRequest request,
      String absolutePluginRequestPath, String relativePluginRequestPath, PrintWriter out,
      Locale languageUI) {
    
    super.getJavascriptCSS(request, absolutePluginRequestPath, relativePluginRequestPath, out, languageUI);
    
    out.println("<script src=\"" + relativePluginRequestPath + "js/registroCompulsaExt.js\" type=\"text/javascript\"></script>");
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

      } else if (query.startsWith(JS)) {

        retornarRecursLocal(absolutePluginRequestPath, relativePluginRequestPath, scanWebID,
            query, request, response, languageUI);

      } else if (query.startsWith(CSV_PAGE)) {
        
        csvPage(absolutePluginRequestPath, relativePluginRequestPath, scanWebID, query,
            request, response, fullInfo, languageUI);
        
      } else if (query.startsWith(FINALPAGE)) {

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

    try {
    
      boolean debug = isDebug();
  
      PrintWriter out;
      out = generateHeader(request, response, absolutePluginRequestPath,
          relativePluginRequestPath, languageUI);

      String tstyle = debug? "border: 2px solid red":"";

      out.println("  <table style=\"min-height:200px;width:100%;height:100%;" + tstyle + "\">");

      // ----------------  FILA DE INFORMACIO DE FITXERS ESCANEJATS
      
      out.println("  <tr valign=\"middle\">");
      out.println("    <td align=\"center\">");
      /*
      out.println("      <h3 style=\"padding:5px\">" + getTraduccio("llistatescanejats", languageUI) + "</h3>");
      */
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

      //out.println("    <h1>Sistema de digitalització - Exemple</h1>");
      out.println("");
      out.println("");
      out.println("    <div id=\"addDefaultInput\">");
      out.println("    ");
      out.println("        <form id=\"documentoElectronico\"  action=\"\" method=\"post\">");
      out.println("        <table border=\"0px\">");
      
      String codiIntegracio = getCodiIntegracio();
      
      
      EntidadesWSService api;
      
      api = getEntidadesWSServiceApi(getWSUrlBase(),
          getWSUsername(), getWSPassword());

      Entidad entidad;
      if (codiIntegracio == null) {
        entidad = getEntidadDefault(api);
        if (entidad == null) {
          throw new Exception("No es troba l'entitat/integració per defecte.");
        }
      } else {
        entidad = getEntidadByCodi(api, codiIntegracio);
        if (entidad == null) {
          throw new Exception("No es troba l'entitat/integració amb codi " + codiIntegracio);
        }
      }
      
      
      List<MetaDato> metadades = new ArrayList<MetaDato>();
      metadades.addAll(entidad.getMetaDatos());
      
      
      // Incloure Metadades entitat per defecte ?
      if (entidad.isIncluirMetadatosPorDefecto()) {
        
        Entidad entidadDefault = getEntidadDefault(api);
        
        if (entidadDefault == null) {
          log.warn("No s'ha pogut recuperar l'entitat per defecte.", new Exception());
        } else {
          
          metadades.addAll(entidadDefault.getMetaDatos());
          
        }
        
      }
      
      
      Collections.sort(metadades, new Comparator<MetaDato>() {

        @Override
        public int compare(MetaDato o1, MetaDato o2) {
          return o2.getOrden() - o1.getOrden();
        }
      });
      
      
      for (MetaDato metaDato : metadades) {
        
        switch((int)metaDato.getTipoMetaDato()) {
           case 1: // Select
           {
             String nombre = metaDato.getNombreMetaDato();
             out.println("          <tr>");
             out.println("            <td align=\"right\">"
                 + "<label for='_SDC_" +nombre + "'>" + metaDato.getLabelMetaDato() + "</label>&nbsp;&nbsp;</td>");
             out.println("            <td><select id='_SDC_" +nombre + "' name='_SDC_" +nombre + "' style='width: 200px;'>");
             
             for (ValorMetaDato entry : metaDato.getValoresMetaDatos()) {
               out.println("              <option value='" + entry.getClave()+ "'>" + entry.getValor()+ "</option>");
             }
             
             //out.println("              <option value='1'>Administración</option>");
             //out.println("              <option value='0'>Ciudadano</option>");
             out.println("            </select></td>");
             out.println("          </tr>");
           }
           break;
           
           case 2: // Textbox
             String nombre = metaDato.getNombreMetaDato();
             String def = metaDato.getValorDefault();
             if (def == null) {
               def = "";
             } else {
               def = def.replace("\"", "\\\"");
             }

             Integer maxlength =metaDato.getLongitud();
             String maxlengthStr = "";
             if (maxlength == null) {
               maxlengthStr = "";
             } else {
               maxlengthStr = " maxlength='" + maxlength + "' ";
             }
             

             out.println("          <tr>");
             out.println("            <td align=\"right\">"
                 + "<label for='_SDC_" +nombre + "'>" + metaDato.getLabelMetaDato() +  " </label>&nbsp;&nbsp;</td>");
             out.println("            <td><input type='text' id='_SDC_" +nombre + "' name='_SDC_" +nombre + "' value=\"" + def + "\" " + maxlengthStr + "/></td>");
             out.println("          </tr>");
           break;
           
           default:
             throw new Exception("Tipo de Metadato desconegut (" +metaDato.getTipoMetaDato()
                 + ") ");
        }
      }
      
      
     // XYZ ZZZ  Esborrar Bindings 
      
      /* XYZ ZZZ 
      out.println("          <tr>");
      String id = getValorIdentificadorDocumentOrigen();
      out.println("            <td align=\"right\"><label for='_SDC_identificadorDocumentoOrigen'>" + getTraduccio("iddocorigen", languageUI)    + " </label>&nbsp;&nbsp;</td>");
      out.println("            <td><input type='text' id='_SDC_identificadorDocumentoOrigen' name='_SDC_identificadorDocumentoOrigen' value='" + id + "' maxlength='20'/></td>");
      out.println("          </tr>");


      Properties organ = getValues("organ", languageUI); 
      
      
      out.println("          <tr>");
      out.println("            <td align=\"right\"><label for='_SDC_organo'>" + getTraduccio("organ", languageUI)+ "</label>&nbsp;&nbsp;</td>");
      out.println("            <td><select id='_SDC_organo' name='_SDC_organo' style='width: 200px;'>");
      for (java.util.Map.Entry<Object,Object> entry : organ.entrySet()  ) {
        out.println("              <option value='" + entry.getKey()+ "'>" + entry.getValue()+ "</option>");
      }
      
//      out.println("              <option value='A04013518'>DG. Personal Docent</option>");
//      out.println("              <option value='A04013600'>DG Participació i Transparència</option>");
//      out.println("              <option value='A04013511'>DG DE DESARROLLO TECNOLÓGICO</option>");
//      out.println("              <option value='A04003746'>DG Turisme</option>");
      
      out.println("            </select></td>");
      out.println("          </tr>");
      
      
      Properties origen = getValues("origen", languageUI); 
      out.println("          <tr>");
      out.println("            <td align=\"right\"><label for='_SDC_origen'>" + getTraduccio("origen", languageUI) + "</label>&nbsp;&nbsp;</td>");
      out.println("            <td><select id='_SDC_origen' name='_SDC_origen' style='width: 200px;'>");
      
      for (java.util.Map.Entry<Object,Object> entry : origen.entrySet()  ) {
        out.println("              <option value='" + entry.getKey()+ "'>" + entry.getValue()+ "</option>");
      }
      
      //out.println("              <option value='1'>Administración</option>");
      //out.println("              <option value='0'>Ciudadano</option>");
      out.println("            </select></td>");
      out.println("          </tr>");
      
      
      Properties tipoDocumental = getValues("tipusDocumental", languageUI); 
      
      out.println("          <tr>");
      out.println("            <td align=\"right\"><label for='_SDC_tipoDocumental'>" + getTraduccio("tipusdocumental", languageUI) + "</label>&nbsp;&nbsp;</td>");
      out.println("            <td><select id='_SDC_tipoDocumental' name='_SDC_tipoDocumental' style='width: 200px;'>");
      for (java.util.Map.Entry<Object,Object> entry : tipoDocumental.entrySet()  ) {
        out.println("              <option value='" + entry.getKey()+ "'>" + entry.getValue()+ "</option>");
      }
      out.println("            </select></td>");
      out.println("          </tr>");
      out.println("          </table>");
      */

      
      
      if (codiIntegracio != null) {
        out.println("          <tr>");
        out.println("            <td align=\"right\">"
            + "<label for='_SDC_codigo'>" + getTraduccio("codiintegracio", languageUI) + "</label>&nbsp;&nbsp;</td>");
        //out.println("          <td align=\"right\"><label>_SDC_codigo</label>&nbsp;&nbsp;</td>");
        
        String readonly = isDebug()? "" : " readonly";
        out.println("            <td><input type='text' id='_SDC_codigo' name='_SDC_codigo' value='" + codiIntegracio + "' " + readonly + " /></td>");
        out.println("          </tr>");
        out.println("");
      };

      



      // Escanejar
      String escanejar = getTraduccio("escanejar", languageUI);
      out.println("          <tr>");
      out.println("            <td align=\"right\" colspan='2'>");
      out.println("              <br/>");
      out.println("              <input type=\"button\" class=\"btn btn-primary\" value=\"" + escanejar + "!!!\" onclick=\"copiasimple()\" />");
      out.println("            </td>");
      out.println("          </tr>");
      out.println("</table>");
      
      out.println("");
      if (isDebug()) {
        out.println("");
        out.println("          <p>");
        out.println("            <label for='csv'>CSV retornat</label>");
        out.println("            <input type='text' id='csv' name='csv' value=''/>            ");
        out.println("          </p>");
        out.println("");

      } else {
        out.println("            <input type='hidden' id='csv' name='csv' value=''/>            ");
      }
      
      out.println("");
      out.println("");
      out.println("        </form>");
      out.println("        ");
      out.println("        </div>");
      //out.println("<br/><br/>");
      out.println("</center></div>");
          
      out.println("  </td></tr>");
      out.println("</table>");    
  
      
      out.println("<script type=\"text/javascript\">");
      out.println("     function setCsv (csv) {");
      if (isDebug()) {
      out.println("        console.log('Passam per setCsv(' + csv + ')');");    
      }
      out.println("        document.getElementById('csv').value = csv;");
      out.println("        var request;");
      out.println("        if(window.XMLHttpRequest) {");
      out.println("            request = new XMLHttpRequest();");
      out.println("        } else {");
      out.println("            request = new ActiveXObject(\"Microsoft.XMLHTTP\");");
      out.println("        }");
      if (isDebug()) {
        out.println("        console.log('URL = " + absolutePluginRequestPath + CSV_PAGE + "/'+ csv);");    
      }
      out.println("        request.open('GET', '" + absolutePluginRequestPath + CSV_PAGE + "/'+ csv, false);");
      out.println("        request.send();"); 
      
      if (isDebug()) {
        out.println("        console.log('request.status = ' + request.status);");    
      }

      // Errors els mostram per pantall  
      out.println("    if ((request.status + '') != '" + HttpServletResponse.SC_OK + "') {");
      if (isDebug()) {
        out.println("      console.log('request.responseText = ' + request.responseText);");
      }
      out.println("      alert(request.responseText);");
      out.println("    };");
      out.println("}");
      
  
      
      
      
  
      out.println("  function getHostPort() {");
      
      String hostport;
      hostport = "location.protocol + '//' + location.host"; // location.host conte ip i port
      /*
      String urlBase = getWSUrlBase();
      
      if (urlBase == null) {
        hostport = "location.protocol + '//' + location.host"; // location.host conte ip i port
      } else {
        URL url = new URL(urlBase);
        hostport = "'" + url.getProtocol()+ "://" + url.getHost() + ":" + url.getPort() + "'";
      }
      */
      

      if (debug) {
        out.println("    alert('getHostPort=' + " + hostport +" );");
      }
      out.println("    return " + hostport + ";");
      out.println("  }");
      out.println();
      
  
       
       if ((fullInfo.getMode() == ScanWebMode.SYNCHRONOUS))  { 
         out.println("  function finalScanProcess() {");
         out.println("    if (document.getElementById(\"escanejats\").innerHTML.indexOf(\"ajax\") !=-1) {");
         out.println("      if (!confirm('" + getTraduccio("noenviats", languageUI) +  "')) {");
         out.println("        return;");
         out.println("      };");
         out.println("    };");
         out.println("    location.href=\"" + relativePluginRequestPath   + FINALPAGE + "\";");
         out.println("  }\n");
         out.println();
//         out.println("    if ((request.status + '') == '" + HttpServletResponse.SC_OK + "') {");
//         out.println("      clearTimeout(myTimer);");
//         out.println("      window.location.href = '" + fullInfo.getUrlFinal() + "';");
//         out.println("    } else {");
//         out.println("      alert(request.response);");
//         out.println("    }");
       }
       
  
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
      out.println("      document.getElementById(\"escanejats\").innerHTML = '" + getTraduccio("docsPujats", languageUI) + ":' + request.responseText;");
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
    
    } catch(Exception e) {
      
      log.error("Error en indexPage(); " + e.getMessage(), e);
      
      // TODO traduir
      String msg = "s'ha produit un error precessant la pàgina index.html:" + e.getMessage();
      
      String urlfinal = fullInfo.getUrlFinal();
      
      fullInfo.getStatus().setStatus(ScanWebStatus.STATUS_FINAL_ERROR);
      
      processCriticalError(response, fullInfo, msg, e, urlfinal);
    }
    

  }
  
  
  public static Entidad getEntidadDefault(
      EntidadesWSService api) {
    
    Entidad entidadDefault = new Entidad();
    entidadDefault.setEsDefault("1");
    
    List<Entidad> listEntidades;
    listEntidades = api.getEntidadDynamic(entidadDefault);
    if (listEntidades==null || listEntidades.isEmpty()) {
       return null;
    } else {
       return (Entidad)listEntidades.get(0);
    }

  }
  
  public static Entidad getEntidadByCodi(
      EntidadesWSService api, String codigo) {
    
    Entidad entidadByCodigo = new Entidad();
    entidadByCodigo.setCodigo(codigo);
    
    List<Entidad> listEntidades;
    listEntidades = api.getEntidadDynamic(entidadByCodigo);
    if (listEntidades==null || listEntidades.isEmpty()) {
       return null;
    } else {
       return (Entidad)listEntidades.get(0);
    }

  }

  
  

  private void processCriticalError(HttpServletResponse response, ScanWebConfig fullInfo,
      String msg, Exception e, String urlfinal) {
    fullInfo.getStatus().setErrorMsg(msg);
    fullInfo.getStatus().setErrorException(e);
    log.error(msg , e);
    sendRedirect(response, urlfinal);
  }

  public int getWidth() {
    return 550;
  }
  
  
  
  
//----------------------------------------------------------------------------
 // ----------------------------------------------------------------------------
 // ------------------------------  CSV PAGE   ------------------------------
 // ----------------------------------------------------------------------------
 // ----------------------------------------------------------------------------
 
 protected static final String CSV_PAGE = "csv";

 
 protected void csvPage(String absolutePluginRequestPath, String relativePluginRequestPath,
     long scanWebID, String query, HttpServletRequest request, HttpServletResponse response,
     ScanWebConfig fullInfo, Locale languageUI) {
   
   final boolean debuglog = log.isDebugEnabled();

   if (debuglog) { 
     log.debug("  QUERY = " + query);
   }
   int index =  query.indexOf(CSV_PAGE);
   String csv = query.substring(index + CSV_PAGE.length() + 1);
   if (debuglog) { 
     log.debug(" CSV = " + csv);
   }

   try {
     
     String urlBase = getWSUrlBase();
     if (debuglog) { 
       log.debug("urlBase = " + urlBase);
       log.debug("user = " + getWSUsername());
       log.debug("password = " + getWSPassword());
     }

     
     CopiaAutenticaWSService apiCA;
    
     apiCA = getCopiaAutenticaWSServiceApi(urlBase,
         getWSUsername(), getWSPassword());
  
     
     DocumentoElectronico docEle = apiCA.obtenerCopiaAutentica(csv);
     
     
     if (isDebug()) {
       log.info(documentoElectronicoToString(docEle));
     }
     
     List<Metadata> metadatas = getMetadades(docEle);   
     
     final Date date = new Date(System.currentTimeMillis());

     DatosDocumento datos = docEle.getDatos();
     
     String name = datos.getNombre();
     if (name == null) {
       name = "signed.pdf";
     }
     final String mime = "application/pdf";
     
     
     final Boolean attachedDocument = null; // No te sentit
     ScannedSignedFile signedScanFile = new ScannedSignedFile(name, mime, datos.getDatos(),
         ScannedSignedFile.PADES_SIGNATURE, attachedDocument);
     
     
     //ScannedPlainFile singleScanFile = new ScannedPlainFile(name, mime, data);
     ScannedDocument scannedDoc = new ScannedDocument();
     scannedDoc.setMetadatas(metadatas);
     scannedDoc.setScannedSignedFile(signedScanFile);
     scannedDoc.setScanDate(date);
     scannedDoc.setScannedPlainFile(null);

     fullInfo.getScannedFiles().add(scannedDoc);

     // XYZ TODO Hauria de retornar el PDF dins firma electronica
     /*
     FirmaElectronica firma = docEle.getFirma();
     
     if (firma != null && firma.getBytesFirma() != null) {
        System.out.println("firma.getBytesFirma().length = " + firma.getBytesFirma().length);
     } else {
       System.out.println("firma.getBytesFirma() = NULL");
     }
     */

     response.setStatus(HttpServletResponse.SC_OK);
     response.getWriter().println("OK_" + fullInfo.getScannedFiles().size());
   } catch(Exception e) {
     
     log.error("Error en csv Page " + e.getMessage(), e);
     
     // Ajax
     String msg = "s'ha produit un error al descarregar el document a partir "
         + "del csv (" + csv + "):" + e.getMessage();
     
     
    try {
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.getWriter().println(msg);
    } catch (IOException e1) {
      e1.printStackTrace();
    }
     
   }
   
   
  
 }
 
 
 
  protected String documentoElectronicoToString(DocumentoElectronico docEle) {
    
    
   MetadatosDocumentoElectronico metas = docEle.getMetadatos();
  

    
    StringBuffer str = new StringBuffer();

    DatosDocumento datos = docEle.getDatos();
    
    str.append("datos.getDatos().length = " + datos.getDatos().length);
    
    
    str.append("\ndatos.getExtension() = " + datos.getExtension());
    str.append("\ndatos.getNombre() = " +datos.getNombre());
    
    str.append("\nmetas.getCSV(): " +       metas.getCSV());
    

    str.append("\nmetas.getNombreFormato() = " + metas.getNombreFormato());
    //
    str.append("\nmetas.getTipoDocumentoCAIB() = " + metas.getTipoDocumentoCAIB());
    str.append("\nmetas.getUsuario() = " + metas.getUsuario());

    
    
    EniMetadata eni = metas.getEniMetadata();
    
    str.append("\neni.getEstadoElaboracion(): " +eni.getEstadoElaboracion());
    str.append("\neni.getFechaCaptura(): " +eni.getFechaCaptura());
    str.append("\neni.getIdentificador(): " +eni.getIdentificador());
    for(String org : eni.getOrgano()) {
      str.append("\neni.getOrgano(): " + org);
    }
    str.append("\neni.getOrigenCiudadanoAdministracion(): " +eni.getOrigenCiudadanoAdministracion());
    str.append("\neni.getTipoDocumental(): " +eni.getTipoDocumental());
    str.append("\neni.getVersionNTI(): " +eni.getVersionNTI());
    
    str.append("\nmetas.getFechaCaducidad(): " +metas.getFechaCaducidad());
    
    
    InformacionDocumento infoDoc = metas.getInformacionDocumento();
    
    str.append("\ninfoDoc.getIdDocGestorDocumental(): " +infoDoc.getIdDocGestorDocumental());
    str.append("\ninfoDoc.getIdDocTemporal(): " +infoDoc.getIdDocTemporal());
    str.append("\ninfoDoc.getIdEntidad(): " +infoDoc.getIdEntidad());
    str.append("\ninfoDoc.getIdTipoFormatoDocEntrada(): " +infoDoc.getIdTipoFormatoDocEntrada());
    str.append("\ninfoDoc.getUrlDocumento(): " +infoDoc.getUrlDocumento());
    
    
    LabelMetadatosComplementarios compl = metas.getLabelMetadatosComplementarios();
    
    List<Entry> list = compl.getEntry();
    
    for (Entry entry : list) {
      str.append("\nmetas.getLabelMetadatosComplementarios()[" + entry.getKey() + "] => " + entry.getValue());
    }
    
    
    MetadatosComplementarios complVal = metas.getMetadatosComplementarios();
    
    List<es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios.Entry> listVal;
    listVal = complVal.getEntry();
    
    for (es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios.Entry entry : listVal) {
      str.append("\nmetas.getMetadatosComplementarios()[" + entry.getKey() + "] => " + entry.getValue());
    }
    
    
    MetadatosFirmaElectronica metaSign = metas.getMetadatosFirmaDocOriginal();
    
    str.append("\nmetaSignDocOriginal.getAccionFirmanteSobreDocumento() = " + metaSign.getAccionFirmanteSobreDocumento());
    str.append("\nmetaSignDocOriginal.getAsunto = " + metaSign.getAsunto());
    str.append("\nmetaSignDocOriginal.getEmisorCertificado() = " + metaSign.getEmisorCertificado());
    str.append("\nmetaSignDocOriginal.getIdVersionPoliticaFirma() = " + metaSign.getIdVersionPoliticaFirma());
    
    str.append("\nmetaSignDocOriginal.getFechaFirma() = " + metaSign.getFechaFirma());
    
    str.append("\nmetaSignDocOriginal.getNombreCompletoFirmante() = " + metaSign.getNombreCompletoFirmante());
    str.append("\nmetaSignDocOriginal.getNumeroSerie() = " + metaSign.getNumeroSerie());
    str.append("\nmetaSignDocOriginal.getRef() = " + metaSign.getRef());
    str.append("\nmetaSignDocOriginal.getRolFirmante() = " + metaSign.getRolFirmante());
    str.append("\nmetaSignDocOriginal.getTipoFirma(). = " +  metaSign.getTipoFirma());
    str.append("\nmetaSignDocOriginal.getTipoFirmaOriginal() = " +   metaSign.getTipoFirmaOriginal());
    
    
    {
      EniContenidoFirma eniFirm = metaSign.getContenidoFirma();
      str.append("\nmetaSignDocOriginal.ContenidoFirma.getCsv().getRegulacionGeneracionCSV(): " + eniFirm.getCsv().getRegulacionGeneracionCSV());
      str.append("\nmetaSignDocOriginal.ContenidoFirma.getCsv().getValorCSV(): " +eniFirm.getCsv().getValorCSV());
  
      str.append("\nmetaSignDocOriginal.ContenidoFirma.getFirmaConCertificado().getReferenciaFirma():  " +eniFirm.getFirmaConCertificado().getReferenciaFirma());
  
      // convertir List<Byte> a byte[]
      
      List<Byte> listBytes = eniFirm.getFirmaConCertificado().getFirmaBase64();
      if (listBytes != null && listBytes.size() != 0) {
        str.append("\nmetaSignDocOriginal.ContenidoFirma.getFirmaConCertificado().getFirmaBase64() = ");
        for (Byte sign : listBytes ) {
          str.append((char)(byte)sign);  
        }
      }
    }
    
    

    List<MetadatosFirmaElectronica> firmes = metas.getMetadatosFirmas();
    
    if (firmes == null || firmes.size() == 0) {
      str.append("\nmetas.getMetadatosFirmas() = NULL");
    } else {
      int count = 0;
      for (MetadatosFirmaElectronica f : firmes) {
        String base = "\nmetas.getMetadatosFirmas[" + count + "].";
        str.append(base + "getAccionFirmanteSobreDocumento(): " + f.getAccionFirmanteSobreDocumento());
        
        str.append("\nmetas.getMetadatosFirmas[" + count + "]." + f.getAsunto());

        str.append("\nmetas.getMetadatosFirmas[" + count + "].getEmisorCertificado()" + f.getEmisorCertificado());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getIdVersionPoliticaFirma()" + f.getIdVersionPoliticaFirma());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getNombreCompletoFirmante()" + f.getNombreCompletoFirmante());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getNumeroSerie()" + f.getNumeroSerie());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getRef()" + f.getRef());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getRolFirmante()" + f.getRolFirmante());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getContenidoFirma()" + f.getContenidoFirma());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getContenidoFirma()" + f.getContenidoFirma());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getTipoFirma()" + f.getTipoFirma());
        str.append("\nmetas.getMetadatosFirmas[" + count + "].getTipoFirmaOriginal()" + f.getTipoFirmaOriginal());

        {
          EniContenidoFirma eniFirm = f.getContenidoFirma();
          str.append("\nmetas.getMetadatosFirmas[" + count + "].ContenidoFirma.getCsv().getRegulacionGeneracionCSV(): " + eniFirm.getCsv().getRegulacionGeneracionCSV());
          str.append("\nmetas.getMetadatosFirmas[" + count + "].ContenidoFirma.getCsv().getValorCSV(): " +eniFirm.getCsv().getValorCSV());
      
          str.append("\nmetas.getMetadatosFirmas[" + count + "].ContenidoFirma.getFirmaConCertificado().getReferenciaFirma():  " +eniFirm.getFirmaConCertificado().getReferenciaFirma());
      
          // convertir List<Byte> a byte[]
          
          List<Byte> listBytes = eniFirm.getFirmaConCertificado().getFirmaBase64();
          if (listBytes != null && listBytes.size() != 0) {
            str.append("\nmetas.getMetadatosFirmas[" + count + "].ContenidoFirma.getFirmaConCertificado().getFirmaBase64() = ");
            for (Byte sign : listBytes ) {
              str.append((char)(byte)sign);  
            }
          }
        }
        
      }
      
    }
    
   
    FirmaElectronica firma = docEle.getFirma();
    
    if (firma != null && firma.getBytesFirma() != null) {
      str.append("\nfirma.getBytesFirma().length = " + firma.getBytesFirma().length);
    } else {
      str.append("\nfirma.getBytesFirma() = NULL");
    }
    
    
    
    str.append("\n");
    return str.toString();
  }
 
 
  
  public List<Metadata> getMetadades(DocumentoElectronico docEle) {
    
    MetadatosDocumentoElectronico metas =  docEle.getMetadatos();
    
    List<Metadata> metadatas = new ArrayList<Metadata>();
    
    // ============  0.- generals ===================
    
    // metas.getCSV(): hhpmlhdfENowPYEYhLsw
    // metas.getFechaCaducidad(): 2018-06-28 13:55:34.131
    // metas.getNombreFormato() = PDF
    // metas.getTipoDocumentoCAIB() = Còpia simple de document en paper
    // metas.getUsuario() = anadal
    // datos.getDatos().length = 1601762
    // datos.getExtension().length = null
    // datos.getNombre().length = null

    addMeta(metadatas, "CSV", metas.getCSV());
    addMeta(metadatas, "FechaCaducidad", metas.getFechaCaducidad());
    addMeta(metadatas, "NombreFormato", metas.getNombreFormato());
    addMeta(metadatas, "TipoDocumentoCAIB", metas.getTipoDocumentoCAIB());
    addMeta(metadatas, "Usuario", metas.getUsuario());
    DatosDocumento datosDoc = docEle.getDatos();
    if (datosDoc != null) {
      addMeta(metadatas, "Datos.Nombre", datosDoc.getNombre());
      addMeta(metadatas, "Datos.Extension", datosDoc.getExtension());
    }
    
    
    
    // ============  1.- ENI ===================
    //    eni.getEstadoElaboracion(): es.caib.digital.ws.api.copiaautentica.EniEstadoElaboracion@7215fb38
    //    eni.getFechaCaptura(): 2016-06-28 13:55:34.131
    //    eni.getIdentificador(): ES_A04003003_2016_000000000000000000000000000060
    //    eni.getOrgano(): Entidad 1
    //    eni.getOrigenCiudadanoAdministracion(): null
    //    eni.getTipoDocumental(): null
    //    eni.getVersionNTI(): http://administracionelectronica.gob.es/ENI/XSD/v1.0/documento-e
    EniMetadata eni = metas.getEniMetadata();

    final String tipusDocumental = getTipoDocumental(eni.getTipoDocumental());
    if (tipusDocumental != null) {
      metadatas.add(new Metadata("TipoDocumental", tipusDocumental));
    }
    
    
    
    metadatas.addAll(getEstadoElaboracion(eni.getEstadoElaboracion()));
    metadatas.add(new Metadata("Identificador", eni.getIdentificador()));
    // metadatas.add(new Metadata("FechaCaptura", eni.getFechaCaptura()));
    addMeta(metadatas, "FechaCaptura", eni.getFechaCaptura());
    
    metadatas.add(new Metadata("VersionNTI", eni.getVersionNTI()));
    for(String org : eni.getOrgano()) {
      metadatas.add(new Metadata("Organo", org));
    }
    
    EniEnumOrigenCreacion e = eni.getOrigenCiudadanoAdministracion();
    if (e != null) {
      metadatas.add(new Metadata("OrigenCiudadanoAdministracion", e == EniEnumOrigenCreacion.ORIGEN_CREACION_CIUDADANO));
    }

    
    // ============  2.- InfoDoc ===================
    //    infoDoc.getIdDocGestorDocumental(): bb01f0e4-62b9-4799-b67e-2e8dab767d34
    //    infoDoc.getIdDocTemporal(): 61
    //    infoDoc.getIdEntidad(): 1
    //    infoDoc.getIdTipoFormatoDocEntrada(): application/pdf
    //    infoDoc.getUrlDocumento(): null

    InformacionDocumento infoDoc = metas.getInformacionDocumento();
    
    addMeta(metadatas, "InformacionDocumento.IdDocGestorDocumental",infoDoc.getIdDocGestorDocumental());
    addMeta(metadatas, "InformacionDocumento.IdDocTemporal",infoDoc.getIdDocTemporal());
    addMeta(metadatas, "InformacionDocumento.IdEntidad",infoDoc.getIdEntidad());
    addMeta(metadatas, "InformacionDocumento.IdTipoFormatoDocEntrada",infoDoc.getIdTipoFormatoDocEntrada());
    addMeta(metadatas, "InformacionDocumento.UrlDocumento",infoDoc.getUrlDocumento());
    
    
    // ============  3.- Metadatos Complementarios ===================
    //  metas.getLabelMetadatosComplementarios()[ENTRy]: codigo => _SDC_codigo
    //  metas.getLabelMetadatosComplementarios()[ENTRy]: identificadorDocumentoOrigen => Identificador document origen
    //  metas.getLabelMetadatosComplementarios()[ENTRy]: organo => Organ
    //  metas.getLabelMetadatosComplementarios()[ENTRy]: tipoDocumental => Tipus documental
    //  metas.getLabelMetadatosComplementarios()[ENTRy]: origen => Origen
    //  metas.getMetadatosComplementarios()[ENTRy]: identificadorDocumentoOrigen => Identificador documento
    //  metas.getMetadatosComplementarios()[ENTRy]: organo => A04013518
    //  metas.getMetadatosComplementarios()[ENTRy]: origen => 1
    //  metas.getMetadatosComplementarios()[ENTRy]: tipoDocumental => TD01
    //  metas.getMetadatosComplementarios()[ENTRy]: codigo => FUNDACIO_BIT
    
    LabelMetadatosComplementarios compl = metas.getLabelMetadatosComplementarios();
    
    List<Entry> list = compl.getEntry();
    
    for (Entry entry : list) {

      addMeta(metadatas, "MetadatosComplementarios." + entry.getKey() + ".label", entry.getValue());
    }
    
    
    MetadatosComplementarios complVal = metas.getMetadatosComplementarios();
    
    List<es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios.Entry> listVal;
    listVal = complVal.getEntry();
    
    for (es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios.Entry entry : listVal) {
      addMeta(metadatas, "MetadatosComplementarios." + entry.getKey(), entry.getValue());
    }
    
    
    
    // ============  4.- Metadatos FIRMA ELECTRONICA ===================
    //    metaSign.getAccionFirmanteSobreDocumento() = null
    //    metaSign.getAsunto = null
    //    metaSign.getEmisorCertificado() = null
    //    metaSign.getIdVersionPoliticaFirma() = null
    //    metaSign.getFechaFirma() = null
    //    metaSign.getNombreCompletoFirmante() = null
    //    metaSign.getNumeroSerie() = null
    //    metaSign.getRef() = null
    //    metaSign.getRolFirmante() = null
    //    metaSign.getTipoFirma() = null
    //    metaSign.getTipoFirmaOriginal() = null
    
    
    MetadatosFirmaElectronica metaSign = metas.getMetadatosFirmaDocOriginal();

    processMetadatosFirmaElectronica("MetadatosFirmaDocOriginal", metadatas, metaSign);

    List<MetadatosFirmaElectronica> firmes = metas.getMetadatosFirmas();
    
    if (firmes != null && firmes.size() != 0) {
    
      int count = 0;
      for (MetadatosFirmaElectronica f : firmes) {
        String base = "MetadatosFirma." + count;
        
        processMetadatosFirmaElectronica(base, metadatas, f);
        
      }
      
    }

    return metadatas;

  }
    
  
  
  

  private void processMetadatosFirmaElectronica( String base, List<Metadata> metadatas,
      MetadatosFirmaElectronica metaSign) {
    
 
    addMeta(metadatas, base + ".AccionFirmanteSobreDocumento", metaSign.getAccionFirmanteSobreDocumento());
    addMeta(metadatas, base + ".Asunto", metaSign.getAsunto());
    addMeta(metadatas, base + ".EmisorCertificado", metaSign.getEmisorCertificado());
    addMeta(metadatas, base + ".IdVersionPoliticaFirma", metaSign.getIdVersionPoliticaFirma());
    
    addMeta(metadatas, base + ".FechaFirma", metaSign.getFechaFirma());
    
    addMeta(metadatas, base + ".NombreCompletoFirmante", metaSign.getNombreCompletoFirmante());
    addMeta(metadatas, base + ".NumeroSerie", metaSign.getNumeroSerie());
    addMeta(metadatas, base + ".Ref", metaSign.getRef());
    addMeta(metadatas, base + ".RolFirmante", metaSign.getRolFirmante());
    
    
    EniContenidoFirma eniFirm = metaSign.getContenidoFirma();
    
    if (eniFirm != null) {
      if (eniFirm.getCsv() != null) {
           addMeta(metadatas, base + ".ContenidoFirma.CSV.RegulacionGeneracionCSV", eniFirm.getCsv().getRegulacionGeneracionCSV());
         addMeta(metadatas, base + ".ContenidoFirma.CSV.ValorCSV()", eniFirm.getCsv().getValorCSV());
      }
      
      EniFirmaConCertificado efcc = eniFirm.getFirmaConCertificado();
      if (efcc != null) {
         if (efcc.getReferenciaFirma() != null) {
           addMeta(metadatas, base + ".ContenidoFirma.FirmaConCertificado.ReferenciaFirma",
             String.valueOf(efcc.getReferenciaFirma()));
         }
         // Passar de Byte[] a byte[]
         List<Byte> Bytes = efcc.getFirmaBase64();
         if (Bytes != null && Bytes.size() != 0) {
           byte[] bytes = new byte[Bytes.size()];
           int count = 0;
           for (Byte b : Bytes) {
             bytes[count] = b;
             count++;
           }
           metadatas.add(new Metadata( base + ".ContenidoFirma.FirmaConCertificado.FirmaBase64",
               new String(bytes), MetadataType.BASE64));            
         }
      }
    }
    

    EniEnumTipoFirma eetf = metaSign.getTipoFirma();
    
    if (eetf != null) {
      String tipoFirma = null;
      
      switch(eetf) {
        case TIPO_FIRMA_CSV: tipoFirma = "TF01"; break;
        case TIPO_FIRMA_XADES_INTERNALLY_DETACHED_SIGNATURE : tipoFirma = "TF02"; break;
        case TIPO_FIRMA_XADES_ENVELOPED_SIGNATURE: tipoFirma = "TF03"; break;
        case TIPO_FIRMA_CADES_DETACHED_EXPLICIT_SIGNATURE: tipoFirma = "TF04"; break;
        case TIPO_FIRMA_CADES_ATTACHED_IMPLICIT_SIGNATURE: tipoFirma = "TF05"; break;
        case TIPO_FIRMA_PADES: tipoFirma = "TF06"; break;
      }
    
      if (tipoFirma != null) {
        addMeta(metadatas, base + ".TipoFirma", tipoFirma);
      }
    }
    
    
    TipoFirmaOriginal tfo = metaSign.getTipoFirmaOriginal();
    if (tfo != null) {
      addMeta(metadatas, base + ".TipoFirmaOriginal", tfo.value());
    }
  }
  
  
  public void addMeta(List<Metadata> metadatas, String key, XMLGregorianCalendar gc) {
    
    if (gc != null) {
      Timestamp timestamp = new Timestamp(gc.toGregorianCalendar().getTimeInMillis());
      metadatas.add(new Metadata(key, timestamp));     
    }
    
  }
  
  
  public void addMeta(List<Metadata> metadatas, String key, Timestamp timestamp) {
    
    if (timestamp != null) {
      metadatas.add(new Metadata(key, timestamp));     
    }
    
  }
  
  
 public void addMeta(List<Metadata> metadatas, String key, String value) {
    
    if (value != null) {
      metadatas.add(new Metadata(key, value));     
    }
    
  }
 
 
 public void addMeta(List<Metadata> metadatas, String key, Long value) {
   
   if (value != null) {
     metadatas.add(new Metadata(key, value));     
   }
   
 }
  
  
  
  public List<Metadata> getEstadoElaboracion( EniEstadoElaboracion estadoElaboracion) {
    
    List<Metadata> metas = new ArrayList<Metadata>();
    
   
    if (estadoElaboracion == null) {
      
      //metas.add(new Metadata("EstadoElaboracion", "EE99"));
    } else {
    
      String ido =  estadoElaboracion.getIdentificadorDocumentoOrigen();
      if (ido != null) {
        metas.add(new Metadata("IdentificadorDocumentoOrigen", ido));
      }
  
      EniEnumEstadoElaboracion eeee = estadoElaboracion.getValorEstadoElaboracion();
      if (eeee != null) {
        switch(eeee) {
          case ESTADO_ELABORACION_ORIGINAL:
            metas.add(new Metadata("EstadoElaboracion", "EE01"));
          break;
          case ESTADO_ELABORACION_COPIA_AUTENTICA_CAMBIO_FORMATO:
            metas.add(new Metadata("EstadoElaboracion", "EE02"));
          break;
          case ESTADO_ELABORACION_COPIA_AUTENTICA_DOCUMENTO_PAPEL:
            metas.add(new Metadata("EstadoElaboracion", "EE03"));
          break;
          case ESTADO_ELABORACION_COPIA_PARCIAL_AUTENTICA:
            metas.add(new Metadata("EstadoElaboracion", "EE04"));
          break;        
          case ESTADO_ELABORACION_OTROS:
            metas.add(new Metadata("EstadoElaboracion", "EE99"));
          break;
        }
      }
    }
    
    return metas;
    
  }
  
  
  
  
  public String getTipoDocumental(EniEnumTipoDocumental eniEnumTipoDocumental) {
    

    if (eniEnumTipoDocumental == null) {
      return null;
      //return "TD99";
    }

    switch (eniEnumTipoDocumental) {
      case TIPO_DOCUMENTAL_RESOLUCION:
        return "TD01";
      case TIPO_DOCUMENTAL_ACUERDO:
        return "TD02";
      case TIPO_DOCUMENTAL_CONTRATO:
        return "TD03";
      case TIPO_DOCUMENTAL_CONVENIO:
        return "TD04";
      case TIPO_DOCUMENTAL_DECLARACION:
        return "TD05";
      case TIPO_DOCUMENTAL_COMUNICACION:
        return "TD06";
      case TIPO_DOCUMENTAL_NOTIFICACION:
        return "TD07";
      case TIPO_DOCUMENTAL_PUBLICACION:
        return "TD08";
      case TIPO_DOCUMENTAL_ACUSE_RECIBO:
        return "TD09";
      case TIPO_DOCUMENTAL_ACTA:
        return "TD10";
      case TIPO_DOCUMENTAL_CERTIFICADO:
        return "TD11";
      case TIPO_DOCUMENTAL_DILIGENCIA:
        return "TD12";
      case TIPO_DOCUMENTAL_INFORME:
        return "TD13";
      case TIPO_DOCUMENTAL_SOLICITUD:
        return "TD14";
      case TIPO_DOCUMENTAL_DENUNCUA:
        return "TD15";
      case TIPO_DOCUMENTAL_ALEGACION:
        return "TD16";
      case TIPO_DOCUMENTAL_RECURSOS:
        return "TD17";
      case TIPO_DOCUMENTAL_COMUNICACION_CUIDADANO:
        return "TD18";
      case TIPO_DOCUMENTAL_FACTURA:
        return "TD19";
      case TIPO_DOCUMENTAL_OTROS_INCAUTADOS:
        return "TD20";
     // default:
      case TIPO_DOCUMENTAL_OTROS:
        return "TD99";

    }

    return null;
    
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
      log.error(e.getMessage(), e);
      try {
        response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      
      
    }
  }
  

  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------
  // ---------------------- RECURSOS LOCALS ----------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  public static final String JS = "js/";
  

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
  // --------------- UTILITATS -------------------------------
  // -------------------------------------------------------------------------
  // -------------------------------------------------------------------------

  
  

  public static final String COPIA_AUTENTICA = "ServicioCopiaAutentica"; 
  
  public static final String ENTIDADES = "ServicioEntidades";

  
  public static void configAddressUserPassword(String usr, String pwd, String endpoint,
      Object api) {

    Map<String, Object> reqContext = ((BindingProvider) api).getRequestContext();
    reqContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    reqContext.put(BindingProvider.USERNAME_PROPERTY, usr);
    reqContext.put(BindingProvider.PASSWORD_PROPERTY, pwd);
    
    reqContext.put("disableCNCheck", "true");
  }

  public static CopiaAutenticaWSService getCopiaAutenticaWSServiceApi(String urlBase, String usr, String pwd)
      throws Exception {

    final String endPoint = urlBase +  COPIA_AUTENTICA;
    
    if (log.isDebugEnabled()) {
      log.debug(" Digital WS endPoint = " + endPoint);
    }
    
    String fileName = "wsdl/ServicioCopiaAutentica.wsdl";
    ClassLoader classLoader = CAIBScanWebPlugin.class.getClassLoader();
    URL wsdlLocation = classLoader.getResource(fileName);
    

    CopiaAutenticaWSServiceService srv = new CopiaAutenticaWSServiceService(wsdlLocation);

    CopiaAutenticaWSService api = srv.getCopiaAutenticaWSServiceImplWs();

    configAddressUserPassword(usr, pwd, endPoint, api);

    return api;

  }
  
  
  public static EntidadesWSService getEntidadesWSServiceApi(String urlBase, String usr, String pwd)
      throws Exception {
    

    final String endPoint = urlBase +  ENTIDADES;
    

    String fileName = "wsdl/ServicioEntidades.wsdl";
    ClassLoader classLoader = CAIBScanWebPlugin.class.getClassLoader();
    URL wsdlLocation = classLoader.getResource(fileName);

    EntidadesWSServiceImplService srv = new EntidadesWSServiceImplService(wsdlLocation);

    EntidadesWSService api = srv.getEntidadesWSServiceImplWs();

    configAddressUserPassword(usr, pwd, endPoint, api);

    return api;

  }
  
  


}