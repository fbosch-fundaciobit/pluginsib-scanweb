package org.fundaciobit.plugins.scanweb.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fundaciobit.plugins.utils.Metadata;

/**
 * 
 * @author anadal
 *
 */
public class ScanWebConfig {
  
  protected long scanWebID;

  protected String scanType;
  protected Set<String> flags;
  /**  Es permeten entrades amb claus repetides */
  protected List<Metadata> metadades; 

  protected ScanWebMode mode;

  protected String languageUI;


  /**
   * Opcional. Requerit en mode SINCRON
   */
  protected String urlFinal;
  
  
  protected ScanWebStatus status = new ScanWebStatus();
  
  
  protected List<ScannedDocument> scannedFiles = new ArrayList<ScannedDocument>();
  

  /**
   * 
   */
  public ScanWebConfig() {
    super();
  }



  /**
   * @param scanWebID
   * @param scanType
   * @param flags
   * @param metadades
   * @param mode
   * @param languageUI
   * @param urlFinal
   */
  public ScanWebConfig(long scanWebID, String scanType, Set<String> flags,
      List<Metadata> metadades, ScanWebMode mode, String languageUI, String urlFinal) {
    super();
    this.scanWebID = scanWebID;
    this.scanType = scanType;
    this.flags = flags;
    this.metadades = metadades;
    this.mode = mode;
    this.languageUI = languageUI;
    this.urlFinal = urlFinal;
  }



  public String getScanType() {
    return scanType;
  }

  public void setScanType(String scanType) {
    this.scanType = scanType;
  }

  public Set<String> getFlags() {
    return flags;
  }

  public void setFlags(Set<String> flags) {
    this.flags = flags;
  }

  public List<Metadata> getMetadades() {
    return metadades;
  }

  public void setMetadades(List<Metadata> metadades) {
    this.metadades = metadades;
  }

  public String getUrlFinal() {
    return urlFinal;
  }

  public void setUrlFinal(String urlFinal) {
    this.urlFinal = urlFinal;
  }

  public ScanWebMode getMode() {
    return mode;
  }

  public void setMode(ScanWebMode mode) {
    this.mode = mode;
  }

  public String getLanguageUI() {
    return languageUI;
  }

  public void setLanguageUI(String languageUI) {
    this.languageUI = languageUI;
  }

  public long getScanWebID() {
    return scanWebID;
  }

  public void setScanWebID(long scanWebID) {
    this.scanWebID = scanWebID;
  }

  public ScanWebStatus getStatus() {
    return status;
  }

  public void setStatus(ScanWebStatus status) {
    this.status = status;
  }

  public List<ScannedDocument> getScannedFiles() {
    return scannedFiles;
  }

  public void setScannedFiles(List<ScannedDocument> scannedFiles) {
    this.scannedFiles = scannedFiles;
  }

}
