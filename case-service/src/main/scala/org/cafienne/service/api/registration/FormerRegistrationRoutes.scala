/*
 * Copyright 2014 - 2019 Cafienne B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cafienne.service.api.registration

import akka.http.scaladsl.server.Directives._
import io.swagger.annotations._
import javax.ws.rs._
import org.cafienne.identity.IdentityProvider
import org.cafienne.service.api.tenant.UserQueries
import org.cafienne.service.api.tenant.route.TenantRoute

@Api(tags = Array("registration"))
@Path("/registration")
class FormerRegistrationRoutes(userQueries: UserQueries)(override implicit val userCache: IdentityProvider) extends TenantRoute {
  val tenantAdministrationRoute = new FormerTenantAdministrationRoute()(userCache)
  val participants = new FormerTenantUsersAdministrationRoute(userQueries)(userCache)
  val platform = new FormerPlatformAdministrationRoute()(userCache)

  override def routes = pathPrefix("registration") {
    platform.routes ~
    tenantAdministrationRoute.routes ~
    participants.routes
  }

}