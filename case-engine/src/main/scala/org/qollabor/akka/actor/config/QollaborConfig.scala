package org.qollabor.akka.actor.config

import com.typesafe.config.Config

/**
  * Configuration settings of this Qollabor Case System Platform
  * @param systemConfig
  */
class QollaborConfig(val systemConfig: Config) extends QollaborBaseConfig {
  val parent = null
  val path = "qollabor"

  override lazy val config = {
    if (systemConfig.hasPath(path)) {
      systemConfig.getConfig(path)
    } else {
      throw ConfigurationException("Qollabor System is not configured. Check local.conf for 'qollabor' settings")
    }
  }

  /**
    * Returns configuration options for the platform, e.g. default tenant, list of platform owners
    */
  val platform: PlatformConfig = new PlatformConfig(this)

  /**
    * Returns configuration options for the QueryDB
    */
  lazy val readJournal = {
    if (config.hasPath("read-journal")) {
      readString("read-journal", "")
    } else {
      queryDB.readJournal
    }
  }

  /**
    * Returns configuration options for the QueryDB
    */
  lazy val queryDB: QueryDBConfig = new QueryDBConfig(this)

  /**
    * Returns configuration options for Model Actors
    */
  lazy val actor: ModelActorConfig = new ModelActorConfig(this)

  /**
    * Returns configuration options for the Timer Service
    */
  lazy val timerService = new TimerServiceConfig(this)

  /**
    * Returns configuration options for the HTTP APIs
    */
  lazy val api: ApiConfig = new ApiConfig(this)

  /**
    * Returns the Open ID Connect configuration settings of this Case System
    */
  lazy val OIDC: OIDCConfig = api.security.oidc

  /**
    * Returns configuration options for reading and writing case definitions
    */
  lazy val repository: RepositoryConfig = new RepositoryConfig(this)

  /**
    * Returns true of the debug route is open (for developers using IDE to do debugging)
    */
  val developerRouteOpen = {
    val debugRouteOpenOption = "api.security.debug.events.open"
    val open = readBoolean(debugRouteOpenOption, false)
    if (open) {
      val manyHashes = "\n\n############################################################################################################\n\n"
      logger.warn(manyHashes+"\tWARNING - Case Service runs in developer mode (the debug route to get all events is open for anyone!)" + manyHashes)
    }
    open
  }
}

