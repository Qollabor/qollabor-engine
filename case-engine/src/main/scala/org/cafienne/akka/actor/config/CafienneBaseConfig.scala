package org.cafienne.akka.actor.config

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.FiniteDuration

trait MandatoryConfig extends CafienneBaseConfig {
  override lazy val config = {
    if (parent.config.hasPath(path)) {
      parent.config.getConfig(path)
    } else {
      throw new Exception("Missing config " + path +" in " + parent.config)
    }
  }
}

/**
  * Simple trait to help reading config settings and default values
  * Perhaps something like this already exists.
  */
trait CafienneBaseConfig extends LazyLogging {
  val parent: CafienneBaseConfig
  val path: String
  val exception: Throwable = null

  lazy val config: Config = {
    if (parent.config.hasPath(path)) {
      parent.config.getConfig(path)
    } else {
      null
    }
  }

  def readNumber(path: String, default: Number): Number = {
    if (config != null && config.hasPath(path)) {
      config.getNumber(path)
    } else {
      default
    }
  }

  def readLong(path: String, default: Long): Long = {
    if (config != null && config.hasPath(path)) {
      config.getLong(path)
    } else {
      default
    }
  }

  def readInt(path: String, default: Int): Int = {
    if (config != null && config.hasPath(path)) {
      config.getInt(path)
    } else {
      default
    }
  }

  def readString(path: String, default: String): String = {
    if (config != null && config.hasPath(path)) {
      config.getString(path)
    } else {
      default
    }
  }

  def readBoolean(path: String, default: Boolean): Boolean = {
    if (config != null && config.hasPath(path)) {
      config.getBoolean(path)
    } else {
      default
    }
  }

  def readDuration(name: String, default: FiniteDuration): FiniteDuration = {
    if (config != null && config.hasPath(name)) {
      FiniteDuration(config.getDuration(name).toMillis, TimeUnit.MILLISECONDS)
    } else {
      default
    }
  }
}