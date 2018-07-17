package org.fundaciobit.plugins.scanweb.api;

import java.util.Date;
import java.util.List;

import org.fundaciobit.plugins.utils.Metadata;

/**
 * 
 * @author anadal
 *
 */
public class ScannedDocument {

  protected ScannedPlainFile scannedPlainFile;

  protected ScannedSignedFile scannedSignedFile;

  protected List<Metadata> metadatas;

  protected Date scanDate;

  /**
   * 
   */
  public ScannedDocument() {
    super();
  }

  public ScannedPlainFile getScannedPlainFile() {
    return scannedPlainFile;
  }

  public void setScannedPlainFile(ScannedPlainFile scannedPlainFile) {
    this.scannedPlainFile = scannedPlainFile;
  }

  public ScannedSignedFile getScannedSignedFile() {
    return scannedSignedFile;
  }

  public void setScannedSignedFile(ScannedSignedFile scannedSignedFile) {
    this.scannedSignedFile = scannedSignedFile;
  }

  public List<Metadata> getMetadatas() {
    return metadatas;
  }

  public void setMetadatas(List<Metadata> metadatas) {
    this.metadatas = metadatas;
  }

  public Date getScanDate() {
    return scanDate;
  }

  public void setScanDate(Date scanDate) {
    this.scanDate = scanDate;
  }

}
