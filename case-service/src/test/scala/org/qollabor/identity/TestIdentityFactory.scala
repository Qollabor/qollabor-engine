package org.qollabor.identity

import org.qollabor.akka.actor.identity.{PlatformUser, TenantUser}
import org.qollabor.service.api.projection.record.UserRoleRecord

object TestIdentityFactory {

  def createTenantUser(id: String, tenant: String = "", name: String = "", roles: List[String] = List.empty[String], email: String = "") : TenantUser = {
    TenantUser(id, roles, tenant, name = id, email = email, enabled = true)
  }

  def createPlatformUser(userId: String, tenant: String, roles: Seq[String]) : PlatformUser = {
    PlatformUser(userId, Seq(TenantUser(userId, roles, tenant, name = "", email = "")))
  }

  def asDatabaseRecords(user: TenantUser) : Seq[UserRoleRecord] = {
    var result:Seq[UserRoleRecord] = Seq()
    user.roles.map(role => result = result :+ UserRoleRecord(user.id, user.tenant, user.name, user.email, role, false, true))
    result
  }

  def asDatabaseRecords(user: PlatformUser) : Seq[UserRoleRecord] = {
    var result:Seq[UserRoleRecord] = Seq()
    user.users.map(tenantUser => result = result ++ asDatabaseRecords(tenantUser))
    result
  }

  def asDatabaseRecords(users: Seq[PlatformUser]) : Seq[UserRoleRecord] = {
    var result:Seq[UserRoleRecord] = Seq()
    users.map(user => result = result ++ asDatabaseRecords(user))
    result
  }
}
