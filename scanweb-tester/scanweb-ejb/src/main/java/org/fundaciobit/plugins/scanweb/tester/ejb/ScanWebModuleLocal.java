package org.fundaciobit.plugins.scanweb.tester.ejb;

import java.util.List;
import java.util.Set;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fundaciobit.plugins.scanweb.tester.ejb.utils.ScanWebConfigTester;
import org.fundaciobit.plugins.scanweb.tester.ejb.utils.Plugin;

/**
 * 
 * @author anadal
 *
 */
@Local
public interface ScanWebModuleLocal {
  
  public static final String JNDI_NAME = "scanweb/ScanWebModuleEJB/local";

  public void closeScanWebProcess(HttpServletRequest request, long scanWebID);
  
  
  public void startScanWebProcess(ScanWebConfigTester ess);
  
  
  public String scanDocument(
      HttpServletRequest request, String absoluteRequestPluginBasePath,
      String relativeRequestPluginBasePath,      
      long scanWebID) throws Exception;
  
  
  public void requestPlugin(HttpServletRequest request, HttpServletResponse response,
      String absoluteRequestPluginBasePath, String relativeRequestPluginBasePath,
      long scanWebID, String query, boolean isPost)  throws Exception;
  
  
  public ScanWebConfigTester getScanWebConfig(HttpServletRequest request,
      long scanWebID);
  
  public List<Plugin> getAllPluginsFiltered(HttpServletRequest request, long scanWebID) throws Exception;
  
  
  public Set<String> getDefaultFlags(ScanWebConfigTester ss) throws Exception;
  
}
