package es.caib.digital.ws.test;

import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.fundaciobit.plugins.scanweb.caib.CAIBScanWebPlugin;
import org.fundaciobit.plugins.utils.XTrustProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import es.caib.digital.ws.api.v1.copiaautentica.CopiaAutenticaWSService;
import es.caib.digital.ws.api.v1.copiaautentica.DatosDocumento;
import es.caib.digital.ws.api.v1.copiaautentica.DocumentoElectronico;
import es.caib.digital.ws.api.v1.copiaautentica.EniContenidoFirma;
import es.caib.digital.ws.api.v1.copiaautentica.EniEnumOrigenCreacion;
import es.caib.digital.ws.api.v1.copiaautentica.EniEnumTipoDocumental;
import es.caib.digital.ws.api.v1.copiaautentica.EniEstadoElaboracion;
import es.caib.digital.ws.api.v1.copiaautentica.EniMetadata;
import es.caib.digital.ws.api.v1.copiaautentica.FirmaElectronica;
import es.caib.digital.ws.api.v1.copiaautentica.InformacionDocumento;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.LabelMetadatosComplementarios.Entry;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosFirmaElectronica;
import es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.LabelMetadatosComplementarios;
import es.caib.digital.ws.api.v1.entidades.Entidad;
import es.caib.digital.ws.api.v1.entidades.EntidadesWSService;
import es.caib.digital.ws.api.v1.entidades.MetaDato;
import es.caib.digital.ws.api.v1.entidades.ValorMetaDato;


/**
 *
 * @author anadal
 *
 */
public class Test extends TestUtils {
  
  public static final Logger log = Logger.getLogger(Test.class);

  protected static CopiaAutenticaWSService apiCA;
  
  protected static EntidadesWSService apiE;


  /**
   * S'executa una vegada abans de l'execució de tots els tests d'aquesta classe
   * 
   * @throws Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    XTrustProvider.install();
    
    apiCA = getCopiaAutenticaWSServiceApi(getUserName(), getPassword());
    
    apiE = getEntidadesWSServiceApi(getUserName(), getPassword());
  }

  @AfterClass
  public static void setUpAfterClass() throws Exception {

   
  }


  @org.junit.Test
  public void testCopiaAutentica() throws Exception {

    DatosDocumento datosDoc = new DatosDocumento();
    byte[] data = constructFitxerBeanFromResource("test.pdf");
    System.out.println("data: " + data);

    datosDoc.setDatos(data);
    datosDoc.setNombre("test.pdf");
    datosDoc.setExtension(".pdf");

    long id = System.currentTimeMillis();

    InformacionDocumento infodoc = new InformacionDocumento();
    infodoc.setIdDocTemporal(id);
    infodoc.setIdEntidad(21L);
    infodoc.setIdTipoFormatoDocEntrada("TD01");

    MetadatosFirmaElectronica mfe = new MetadatosFirmaElectronica();

    MetadatosDocumentoElectronico metadatos = new MetadatosDocumentoElectronico();

    metadatos.setIsCarpeta(false);
    metadatos.setInformacionDocumento(infodoc);
    metadatos.setUsuario("u80067");
    metadatos.setTipoDocumentoCAIB("TD01");
    metadatos.setMetadatosFirmaDocOriginal(mfe);
    metadatos.setNombreFormato("application/pdf");

    EniMetadata enimetadata = new EniMetadata();
    enimetadata.setIdentificador(String.valueOf(id));
    enimetadata.setEstadoElaboracion(new EniEstadoElaboracion());


    //enimetadata.setFechaCaptura(new Timestamp(System.currentTimeMillis()));
    GregorianCalendar gcal = new GregorianCalendar();
    gcal.setTimeInMillis(System.currentTimeMillis());
    XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    enimetadata.setFechaCaptura(xgcal);
    
    enimetadata.setOrigenCiudadanoAdministracion(EniEnumOrigenCreacion.ORIGEN_CREACION_CIUDADANO);
    enimetadata.setTipoDocumental(EniEnumTipoDocumental.TIPO_DOCUMENTAL_DECLARACION);
    
    metadatos.setEniMetadata(enimetadata);

    //metadatos.se
    FirmaElectronica firma = new FirmaElectronica();

    DocumentoElectronico doc = new DocumentoElectronico();
    doc.setDatos(datosDoc);
    doc.setFirma(firma);
    doc.setMetadatos(metadatos);

    
    // COPIA AUTENTICA
    /*
    doc = apiCA.generarCopiaAutentica(doc);
        
    byte[] firmat = doc.getFirma().getBytesFirma();
    
    FileUtils.writeByteArrayToFile(new File("firmat.pdf"), firmat);
    
    System.out.println(" FIRMAT == " + firmat);
    */
    
    // COPIA SIMPLE TEMPORAL
    /*
    String result = apiCA.generarCopiaSimpleTemporal(doc);
    
    System.out.println(" RESULT == " + result);
    */
    
    // COPIA TEMPORAL DIGITALIZACION
    String result = apiCA.generarCopiaTemporalDigitalizacion(doc);
    System.out.println(" RESULT == " + result);
    
    System.out.println(" FINAL == ");
       

  }

  @org.junit.Test
  public void testEntidades() throws Exception {
    

    
    List<Entidad> list = apiE.getTodasEntidades();
    
    for (Entidad entidad : list) {
      System.out.println(entidad.getId() + " -> " + entidad.getCodigo());
    }
    
  }
  

  public static void main(String[] args) {
    
    printEntitatDefaultInfo();

    //printEntitatInfoByCodigo();
    
    //printCSVINfo();
    
  }
  
  
  public static void printEntitatInfoByCodigo() {
    try {
      Test test = new Test();
      
      test.setUpBeforeClass();
      
      test.testEntidades();
      
      Entidad entitat = CAIBScanWebPlugin.getEntidadByCodi(apiE, getCodigoEntidad() );  //apiE.getEntidad("10");

      //entitat.getConfiguracionPlantillaEntidad().g

      printInfoEntitat(entitat);

    
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    
  }
  
  
  public static void printEntitatDefaultInfo() {
    try {
      Test test = new Test();
      
      test.setUpBeforeClass();
      
      test.testEntidades();
      
      Entidad entitat = CAIBScanWebPlugin.getEntidadDefault(apiE);

      //entitat.getConfiguracionPlantillaEntidad().g

      printInfoEntitat(entitat);

    
    } catch(Exception e) {
      e.printStackTrace();
    }
    
    
  }
  

  public static void printInfoEntitat(Entidad entitat) {
    List<MetaDato> h = entitat.getMetaDatos();
    
    System.out.println(" ID: " + entitat.getId());
    System.out.println(" Nom: " + entitat.getNombre());
    System.out.println(" CODI: " + entitat.getCodigo());
    
    System.out.println(" isIncluirMetadatosEnPdf: " + entitat.isIncluirMetadatosEnPdf());
    System.out.println(" isIncluirMetadatosPorDefecto: " + entitat.isIncluirMetadatosPorDefecto());
    
    int m = 1;
    for (MetaDato metaDato : h) {
      
      System.out.println();
      System.out.println(" =========== ValorMetaDato [" + (m++) + "] =============== ");
      
      System.out.println("metaDato.getId() = " + metaDato.getId());
      
      System.out.println("metaDato.getIdEntidad() = " + metaDato.getIdEntidad());
      System.out.println("metaDato.getLabelMetaDato() = " + metaDato.getLabelMetaDato());
      System.out.println("metaDato.getLongitud() = " + metaDato.getLongitud());
      System.out.println("metaDato.getNombreMetaDato() = " + metaDato.getNombreMetaDato());
      System.out.println("metaDato.getOrden() = " + metaDato.getOrden());
      System.out.println("metaDato.getTipoMetaDato() = " + metaDato.getTipoMetaDato());
      System.out.println("metaDato.getValorDefault() = " + metaDato.getValorDefault());

      List<ValorMetaDato> values = metaDato.getValoresMetaDatos();
      
      int count = 1;
      for (ValorMetaDato valorMetaDato : values) {
        
        System.out.println("         ------ ValorMetaDato [" + m + "." + (count++) + "] ----- ");
        
        System.out.println("         valorMetaDato.getClave() = " + valorMetaDato.getClave());
        System.out.println("         valorMetaDato.getId() = " + valorMetaDato.getId());
        System.out.println("         valorMetaDato.getIdMetaDato() = " + valorMetaDato.getIdMetaDato());
        System.out.println("         valorMetaDato.getValor() = " + valorMetaDato.getValor());
        
      }

    }
  }
  
  
  
  
  public static void printCSVINfo() {
    
    try {
      Test test = new Test();
      
      test.setUpBeforeClass();
      
      String csv = "NGPfdsxkALS6gTtESG4Z"; //"hhpmlhdfENowPYEYhLsw";
      
      DocumentoElectronico docEle = apiCA.obtenerCopiaAutentica(csv);
      
      DatosDocumento datos = docEle.getDatos();
      
      //FileUtils.writeByteArrayToFile(new File(csv + ".pdf"), datos.getDatos());
      
      System.out.println("datos.getDatos().length = " + datos.getDatos().length);
      
      System.out.println("datos.getExtension().length = " + datos.getExtension());
      System.out.println("datos.getNombre().length = " +datos.getNombre());
      
      
      FirmaElectronica firma = docEle.getFirma();
      
      if (firma != null && firma.getBytesFirma() != null) {
         System.out.println("firma.getBytesFirma().length = " + firma.getBytesFirma().length);
      } else {
         System.out.println("firma.getBytesFirma() = NULL");
      }
      
      MetadatosDocumentoElectronico metas = docEle.getMetadatos();
      
      System.out.println("       metas.getCSV(): " +       metas.getCSV());
      
      EniMetadata eni = metas.getEniMetadata();
      
      System.out.println("       eni.getEstadoElaboracion(): " +eni.getEstadoElaboracion());
      System.out.println("       eni.getFechaCaptura(): " +eni.getFechaCaptura());
      System.out.println("       eni.getIdentificador(): " +eni.getIdentificador());
      for(String org : eni.getOrgano()) {
        System.out.println("       eni.getOrgano(): " + org);
      }
      System.out.println("       eni.getOrigenCiudadanoAdministracion(): " +eni.getOrigenCiudadanoAdministracion());
      System.out.println("       eni.getTipoDocumental(): " +eni.getTipoDocumental());
      System.out.println("       eni.getVersionNTI(): " +eni.getVersionNTI());
      
      
      
      
      
      System.out.println("      metas.getFechaCaducidad(): " +metas.getFechaCaducidad());
      
      
      InformacionDocumento infoDoc = metas.getInformacionDocumento();
      
      
      System.out.println("       infoDoc.getIdDocGestorDocumental(): " +infoDoc.getIdDocGestorDocumental());
      System.out.println("       infoDoc.getIdDocTemporal(): " +infoDoc.getIdDocTemporal());
      System.out.println("       infoDoc.getIdEntidad(): " +infoDoc.getIdEntidad());
      System.out.println("       infoDoc.getIdTipoFormatoDocEntrada(): " +infoDoc.getIdTipoFormatoDocEntrada());
      System.out.println("       infoDoc.getUrlDocumento(): " +infoDoc.getUrlDocumento());
      
      {
        LabelMetadatosComplementarios compl = metas.getLabelMetadatosComplementarios();
        
        List<Entry> list = compl.getEntry();
        
        for (Entry entry : list) {
          System.out.println("       metas.getLabelMetadatosComplementarios()[ENTRy]: " + entry.getKey() + " => " + entry.getValue());
        }
      }
      
      {
        MetadatosComplementarios compl = metas.getMetadatosComplementarios();
        
        List<es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios.Entry> list;
        list = compl.getEntry();
        
        for (es.caib.digital.ws.api.v1.copiaautentica.MetadatosDocumentoElectronico.MetadatosComplementarios.Entry entry : list) {
          System.out.println("       metas.getMetadatosComplementarios()[ENTRy]: " + entry.getKey() + " => " + entry.getValue());
        }
      }
      
      
      MetadatosFirmaElectronica metaSign = metas.getMetadatosFirmaDocOriginal();
      
      System.out.println("metaSign.getAccionFirmanteSobreDocumento() = " + metaSign.getAccionFirmanteSobreDocumento());
      System.out.println("metaSign.getAsunto = " + metaSign.getAsunto());
      System.out.println("metaSign.getEmisorCertificado() = " + metaSign.getEmisorCertificado());
      System.out.println("metaSign.getIdVersionPoliticaFirma() = " + metaSign.getIdVersionPoliticaFirma());
      System.out.println("metaSign.getFechaFirma() = " + metaSign.getFechaFirma());
      System.out.println("metaSign.getNombreCompletoFirmante() = " + metaSign.getNombreCompletoFirmante());
      System.out.println("metaSign.getNumeroSerie() = " + metaSign.getNumeroSerie());
      System.out.println("metaSign.getRef() = " + metaSign.getRef());
      System.out.println("metaSign.getRolFirmante() = " + metaSign.getRolFirmante());
      System.out.println("metaSign.getTipoFirma() = " +  metaSign.getTipoFirma());
      System.out.println("metaSign.getTipoFirmaOriginal() = " +   metaSign.getTipoFirmaOriginal());
      

      
      EniContenidoFirma eniFirm = metaSign.getContenidoFirma();
      System.out.println("eniFirm.getCsv().getRegulacionGeneracionCSV(): " + eniFirm.getCsv().getRegulacionGeneracionCSV());
      System.out.println("eniFirm.getCsv().getValorCSV(): " +eniFirm.getCsv().getValorCSV());
      
      
      
      System.out.println("eniFirm.getFirmaConCertificado().getReferenciaFirma():  " +eniFirm.getFirmaConCertificado().getReferenciaFirma());

      // convertir List<Byte> a byte[]
      for (Byte sign : eniFirm.getFirmaConCertificado().getFirmaBase64()) {
        System.out.println("eniFirm.getFirmaConCertificado().getFirmaBase64()[?]:  " + sign);  
      }
      

      System.out.println("metas.getNombreFormato() = " + metas.getNombreFormato());
      //
      System.out.println("metas.getTipoDocumentoCAIB() = " + metas.getTipoDocumentoCAIB());
      System.out.println("metas.getUsuario() = " + metas.getUsuario());

      
      
      
      //test.testEntidades();
      
      //test.testCopiaAutentica();
      
    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
    
   
  }
  

}
