package org.fundaciobit.plugins.scanweb.api;

import org.fundaciobit.plugins.documentcustody.api.DocumentCustody;

/**
 * 
 * @author anadal
 *
 */
public class ScannedPlainFile extends DocumentCustody {

  /**
   * 
   */
  public ScannedPlainFile() {
    super();
  }

  /**
   * @param dc
   */
  public ScannedPlainFile(DocumentCustody dc) {
    super(dc);
  }

  /**
   * @param name
   * @param data
   */
  public ScannedPlainFile(String name, byte[] data) {
    super(name, data);
  }

  /**
   * @param name
   * @param mime
   * @param data
   */
  public ScannedPlainFile(String name, String mime, byte[] data) {
    super(name, mime, data);
  }

}
