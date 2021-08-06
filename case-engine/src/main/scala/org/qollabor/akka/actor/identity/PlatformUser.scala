package org.qollabor.akka.actor.identity

import org.qollabor.akka.actor.CaseSystem
import org.qollabor.akka.actor.command.exception.{AuthorizationException, MissingTenantException}
import org.qollabor.akka.actor.serialization.Fields
import org.qollabor.akka.actor.serialization.json.ValueMap
import org.qollabor.infrastructure.json.QollaborJson

final case class PlatformUser(userId: String, users: Seq[TenantUser]) extends QollaborJson {
  final def tenants: Seq[String] = users.map(u => u.tenant)

  /**
    * If the user is registered in one tenant only, that tenant is returned.
    * Otherwise, the default tenant of the platform is returned, but it fails when the user is not a member of that tenant
    * @return
    */
  final def defaultTenant: String = {
    if (tenants.length == 1) {
      tenants.head
    } else {
      val configuredDefaultTenant = CaseSystem.config.platform.defaultTenant
      if (configuredDefaultTenant.isEmpty) {
        throw new MissingTenantException("Tenant property must have a value, because ")
      }
      if (!tenants.contains(configuredDefaultTenant)) {
        if (tenants.isEmpty) {
          // Throws an exception that user does not belong to any tenant
          getTenantUser("")
        }
        throw new MissingTenantException("Tenant property must have a value, because user belongs to multiple tenants")
      }
      configuredDefaultTenant
    }
  }

  final def resolveTenant(optionalTenant: Option[String]): String = {
    optionalTenant match {
      case None => defaultTenant // This will throw an IllegalArgumentException if the default tenant is not configured
      case Some(string) => string.isBlank match {
        case true => defaultTenant
        //        case false => optionalTenant.get // Simply give the tenant requested, regardless whether the user is a member in it or not
        case false => getTenantUser(optionalTenant.get).tenant // Simply give the tenant requested, regardless whether the user is a member in it or not
      }
    }
  }

  override def toValue  = {
    val map = new ValueMap(Fields.userId, userId)
    val userList = map.withArray("tenants")
    users.foreach(user => {
      userList.add(user.toValue)
    })
    map
  }

  final def shouldBelongTo(tenant: String) : Unit = users.find(u => u.tenant == tenant).getOrElse(throw AuthorizationException("Tenant '" + tenant +"' does not exist, or user '"+userId+"' is not registered in it"))

  final def isPlatformOwner: Boolean = CaseSystem.isPlatformOwner(userId)

  final def getTenantUser(tenant: String) = users.find(u => u.tenant == tenant).getOrElse({
    val message = tenants.isEmpty match {
      case true => s"User '$userId' is not registered in a tenant"
      case false => s"User '$userId' is not registered in tenant '$tenant'; tenants are: "+tenants.map(tenant => s"'$tenant'").mkString(",")
    }
    throw AuthorizationException(message)
  })
}