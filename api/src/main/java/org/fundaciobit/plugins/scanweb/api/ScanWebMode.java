package org.fundaciobit.plugins.scanweb.api;

/**
 * 
 * @author anadal
 *
 */
public enum ScanWebMode {
  /** No depen del plugin d'escaneig */
  ASYNCHRONOUS,
  /** Existeix un proces o flux en marxa i s'esta pendent dels resultats
   *  del plugin de ScanWeb. Per exemple requereix un boto de cancel o de final 
   */
  SYNCHRONOUS
}
