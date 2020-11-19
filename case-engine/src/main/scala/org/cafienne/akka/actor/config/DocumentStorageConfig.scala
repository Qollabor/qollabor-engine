package org.cafienne.akka.actor.config

import org.cafienne.cmmn.instance.casefile.document.DocumentStorage

class DocumentStorageConfig(val parent: CafienneConfig) extends MandatoryConfig {
  val path = "document-storage"
  override val exception = ConfigurationException("Cafienne Document Storage is not configured. Check local.conf for 'cafienne.document-storage' settings")

  /**
    * DocumentStorage provides an interface for uploading and downloading case file documents
    */
  lazy val DocumentStorage: DocumentStorage = {
    val providerClassName = config.getString("provider")
    Class.forName(providerClassName).getDeclaredConstructor().newInstance().asInstanceOf[DocumentStorage]
  }

  lazy val location: String = {
    config.getString("location")
  }
}