/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.service.api.tenant.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._
import org.qollabor.akka.actor.command.exception.AuthorizationException
import org.qollabor.identity.IdentityProvider
import org.qollabor.service.api.projection.UserSearchFailure
import org.qollabor.service.api.projection.query.UserQueries
import org.qollabor.service.api.tenant.model._

import scala.util.{Failure, Success}


@SecurityRequirement(name = "openId", scopes = Array("openid"))
@Path("/tenant")
class TenantUsersRoute(userQueries: UserQueries)(override implicit val userCache: IdentityProvider) extends TenantRoute {

  override def routes = {
      getTenantUsers ~
      getTenantUser
  }

  @Path("/{tenant}/users")
  @GET
  @Operation(
    summary = "Get tenant users",
    description = "Retrieves the list of tenant users",
    tags = Array("tenant"),
    parameters = Array(
      new Parameter(name = "tenant", description = "The tenant to retrieve users from", in = ParameterIn.PATH, schema = new Schema(implementation = classOf[String]), required = true),
    ),
    responses = Array(
      new ApiResponse(responseCode = "204", description = "List of user ids of that are registered in the tenant", content = Array(new Content(array = new ArraySchema(schema = new Schema(implementation = classOf[TenantAPI.TenantUserFormat]))))),
      new ApiResponse(responseCode = "400", description = "Invalid request"),
      new ApiResponse(responseCode = "500", description = "Not able to perform the action")
    )
  )
  @Produces(Array("application/json"))
  def getTenantUsers = get {
    validUser { platformUser =>
      path(Segment / "users") {
        tenant => runListQuery(userQueries.getTenantUsers(platformUser, tenant))
      }
    }
  }

  @Path("/{tenant}/users/{userId}")
  @GET
  @Operation(
    summary = "Get a tenant user",
    description = "Gets information about the tenant user with the specified id",
    tags = Array("tenant"),
    parameters = Array(
      new Parameter(name = "tenant", description = "The tenant to retrieve users from", in = ParameterIn.PATH, schema = new Schema(implementation = classOf[String]), required = true),
      new Parameter(name = "userId", description = "The user id to read", in = ParameterIn.PATH, schema = new Schema(implementation = classOf[String]), required = true),
    ),
    responses = Array(
      new ApiResponse(responseCode = "204", description = "List of user ids of that are registered in the tenant", content = Array(new Content(array = new ArraySchema(schema = new Schema(implementation = classOf[TenantAPI.TenantUserFormat]))))),
      new ApiResponse(responseCode = "400", description = "Invalid request"),
      new ApiResponse(responseCode = "500", description = "Not able to perform the action")
    )
  )
  @Produces(Array("application/json"))
  def getTenantUser = get {
    validUser { platformUser =>
      path(Segment / "users" / Segment) { (tenant, userId) =>
        onComplete(userQueries.getTenantUser(platformUser, tenant, userId)) {
          case Success(tenantUserInformation) =>
            if (tenantUserInformation.enabled) {
              completeJsonValue(tenantUserInformation.toValue)
            } else {
              // TODO: perhaps this should be allowed for tenant owners?
              if (platformUser.getTenantUser(tenant).isOwner) {
                logger.warn(s"Tenant owner '${platformUser.userId}' tries to fetch tenant user '$userId' but that account has been disabled, hence no response is given")
              } else {
                logger.warn(s"User with id '${platformUser.userId}' tries to fetch tenant user '$userId' but that account has been disabled")
              }
              complete(StatusCodes.NotFound)
            }
          case Failure(failure) =>
            failure match {
              case u: UserSearchFailure => complete(StatusCodes.NotFound, u.getLocalizedMessage)
              case err: AuthorizationException => complete(StatusCodes.Unauthorized, err.getMessage)
              case _ => {
                logger.warn(s"Ran into an exception while getting user '$userId' in tenant '$tenant'", failure)
                complete(StatusCodes.InternalServerError)
              }
            }
        }
      }
    }
  }
}
