package org.qollabor.infrastructure.cqrs

import com.typesafe.scalalogging.LazyLogging

/**
  * Simple factory for getting an offset storage.
  */
trait OffsetStorageProvider extends LazyLogging {
  /**
    * Gets the storage with the given name
    *
    * @param name
    * @return
    */
  def storage(name: String): OffsetStorage
}



