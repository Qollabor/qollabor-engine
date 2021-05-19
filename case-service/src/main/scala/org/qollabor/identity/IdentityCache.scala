package org.qollabor.identity

import com.typesafe.scalalogging.LazyLogging
import org.qollabor.akka.actor.CaseSystem
import org.qollabor.akka.actor.command.exception.AuthorizationException
import org.qollabor.akka.actor.command.response.ActorLastModified
import org.qollabor.akka.actor.identity.{PlatformUser, TenantUser}
import org.qollabor.cmmn.repository.file.SimpleLRUCache
import org.qollabor.service.api.projection.query.UserQueries
import org.qollabor.service.api.tenant.TenantReader

import scala.concurrent.{ExecutionContext, Future}

trait IdentityProvider {
  def getUser(userId: String, tlm: Option[String]): Future[PlatformUser]
  def getUsers(userIds: Seq[String], tenant: String): Future[Seq[TenantUser]] = ???
  def clear(userId: String): Unit
}

class IdentityCache(userQueries: UserQueries)(implicit val ec: ExecutionContext) extends IdentityProvider with LazyLogging {

  // TODO: this should be a most recently used cache
  // TODO: check for multithreading issues now that event materializer can clear.
  private val cache = new SimpleLRUCache[String, PlatformUser](CaseSystem.config.api.security.identityCacheSize)

  def getUser(userId: String, tlm: Option[String]): Future[PlatformUser] = {
    tlm match {
      case Some(s) => {
        // Wait for the TenantReader to be informed about the tenant-last-modified timestamp
        for {
          p <- TenantReader.lastModifiedRegistration.waitFor(new ActorLastModified(s)).future
          u <- executeUserQuery(userId)
        } yield (p, u)._2
      }
      // Nothing to wait for, just continue and execute the query straight on
      case None => executeUserQuery(userId)
    }
  }

  private def executeUserQuery(userId: String): Future[PlatformUser] = {
    cache.get(userId) match {
      case user: PlatformUser => Future(user)
      case null => {
        userQueries.getPlatformUser(userId).map(u => {
          if (u.users.isEmpty && !u.isPlatformOwner) {
            logger.info("User " + userId + " has a valid token, but is not registered in the case system")
            throw AuthorizationException("User " + userId + " is not registered in the case system")
          }
          cache.put(userId, u)
          u
        })
      }
    }
  }

  def clear(userId: String) = {
    // NOTE: We can also extend this to update the cache information, instead of removing keys.
    cache.remove(userId)
  }

  override def getUsers(userIds: Seq[String], tenant: String): Future[Seq[TenantUser]] = {
    userQueries.getSelectedTenantUsers(tenant, userIds)
  }
}
