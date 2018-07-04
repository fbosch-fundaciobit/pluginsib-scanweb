package es.caib.digital.ws.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.fundaciobit.plugins.scanweb.caib.CAIBScanWebPlugin;

import es.caib.digital.ws.api.v1.copiaautentica.CopiaAutenticaWSService;
import es.caib.digital.ws.api.v1.entidades.EntidadesWSService;

/**
 * 
 * @author anadal
 * 
 */
public abstract class TestUtils {

  public static final String COPIA_AUTENTICA = "ServicioCopiaAutentica"; 
  
  public static final String ENTIDADES = "ServicioEntidades";

  private static Properties testProperties = new Properties();

  static {

    // Propietats del Servidor
    try {
      testProperties.load(new FileInputStream("testapi.properties"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getUrlBase() {
    return testProperties.getProperty("test_urlbase");
  }


  public static String getUserName() {
    return testProperties.getProperty("test_usr");
  }

  public static String getPassword() {
    return testProperties.getProperty("test_pwd");
  }
  
  
  public static String getCodigoEntidad() {
    return testProperties.getProperty("UrlBase");
  }
  
  
  
  

  public static CopiaAutenticaWSService getCopiaAutenticaWSServiceApi(String usr, String pwd)
      throws Exception {

    //final String endpoint = getEndPoint(COPIA_AUTENTICA);

    return CAIBScanWebPlugin.getCopiaAutenticaWSServiceApi(getUrlBase(), usr, pwd);

  }
  
  
  public static EntidadesWSService getEntidadesWSServiceApi(String usr, String pwd)
      throws Exception {

    //final String endpoint = getEndPoint(ENTIDADES);

    return CAIBScanWebPlugin.getEntidadesWSServiceApi(getUrlBase(), usr, pwd);

  }


  /**
   *
   * @param name
   * @param mime
   * @return
   * @throws Exception
   */
  public static byte[] constructFitxerBeanFromResource(String name) throws Exception {
    String filename = name;
    if (name.startsWith("/")) {
      filename = name.substring(1);
    } else {
      name = '/' + name;
    }
    InputStream is = TestUtils.class.getResourceAsStream(name);

    return constructFitxerBeanFromInputStream(filename, is);

  }

  public static byte[] constructFitxerBeanFromFile(File file) throws Exception {

    InputStream is = null;
    try {
      is = new FileInputStream(file);
      return constructFitxerBeanFromInputStream(file.getName(), is);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e) {
        }
      }
    }

  }

  public static byte[] constructFitxerBeanFromInputStream(String name, InputStream is)
      throws IOException {

    byte[] data = toByteArray(is);

    return data;
  }

  public static byte[] toByteArray(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    byte[] buffer = new byte[4096];
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
    }
    return output.toByteArray();
  }

}
