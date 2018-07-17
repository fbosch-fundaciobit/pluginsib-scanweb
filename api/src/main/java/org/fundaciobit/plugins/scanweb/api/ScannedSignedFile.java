package org.fundaciobit.plugins.scanweb.api;

import org.fundaciobit.plugins.documentcustody.api.SignatureCustody;

/**
 * 
 * @author anadal
 *
 */
public class ScannedSignedFile extends SignatureCustody {

  /**
   * 
   */
  public ScannedSignedFile() {
    super();
  }

  /**
   * @param sc
   */
  public ScannedSignedFile(SignatureCustody sc) {
    super(sc);
  }

  /**
   * @param name
   * @param data
   * @param signatureType
   * @param attachedDocument
   */
  public ScannedSignedFile(String name, byte[] data, String signatureType,
      Boolean attachedDocument) {
    super(name, data, signatureType, attachedDocument);
  }

  /**
   * @param name
   * @param data
   * @param signatureType
   */
  public ScannedSignedFile(String name, byte[] data, String signatureType) {
    super(name, data, signatureType);
  }

  /**
   * @param name
   * @param mime
   * @param data
   * @param signatureType
   * @param attachedDocument
   */
  public ScannedSignedFile(String name, String mime, byte[] data, String signatureType,
      Boolean attachedDocument) {
    super(name, mime, data, signatureType, attachedDocument);
  }

}
