package org.qollabor.akka.actor.config

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration


class QueryDBConfig(val parent: QollaborConfig) extends MandatoryConfig {
  val path = "query-db"
  override val exception = ConfigurationException("Qollabor Query Database is not configured. Check local.conf for 'qollabor.query-db' settings")

  lazy val restartSettings = new RestartConfig(this)
  lazy val debug = readBoolean("debug", false)
  lazy val readJournal = {
    logger.warn("Obtaining read-journal settings from 'qollabor.querydb.read-journal' is deprecated; please place these settings in 'qollabor.read-journal' instead")
    readString("read-journal", "")
  }
}

class RestartConfig(val parent: QueryDBConfig) extends QollaborBaseConfig {
  val path = "restart-stream"

  lazy val minBackoff = readDuration("min-back-off", FiniteDuration(500, TimeUnit.MILLISECONDS))
  lazy val maxBackoff = readDuration("max-back-off", FiniteDuration(30, TimeUnit.SECONDS))
  lazy val randomFactor = readNumber("random-factor", 0.2).doubleValue()
  lazy val maxRestarts = readInt("max-restarts", 20)
}