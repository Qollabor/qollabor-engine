package org.qollabor.akka.actor.config

class SecurityConfig(val parent: ApiConfig) extends MandatoryConfig {
  val path = "security"
  override val exception = ConfigurationException("Qollabor Security is not configured. Check local.conf for 'qollabor.api.security' settings")

  lazy val oidc: OIDCConfig = new OIDCConfig(this)


  lazy val identityCacheSize = {
    val key = "identity.cache.size"
    val size = readInt(key, 1000)
    if (size == 0) {
      logger.info("Identity Caching is disabled")
    } else {
      logger.info("Running with Identity Cache of size " + size)
    }
    size
  }

}