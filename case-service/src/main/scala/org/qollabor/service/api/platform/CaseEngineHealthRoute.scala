/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.service.api.platform

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import org.qollabor.akka.actor.CaseSystem
import org.qollabor.infrastructure.akka.http.route.CaseServiceRoute

import scala.collection.immutable.Seq

@Path("/")
class CaseEngineHealthRoute() extends CaseServiceRoute {


  // For now, directly in the main, and not as child of PlatformRoutes;
  //  Otherwise, routes are not available when case system is not healthy (because platform routes are AuthenticatedRoute)
  override def routes = { health ~ version ~ status }

  override def apiClasses(): Seq[Class[_]] = {
    Seq(classOf[CaseEngineHealthRoute])
  }

  @Path("/status")
  @GET
  @Operation(
    summary = "Get platform health information as http status code",
    description = "Retrieves the health status information of the Case Engine",
    tags = Array("platform"),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Platform health is ok"),
      new ApiResponse(responseCode = "503", description = "Platform health is not ok")
    )
  )
  def status = get {
    pathPrefix("status") {
      pathEndOrSingleSlash {
        if (CaseSystem.health.ok) {
          complete(StatusCodes.OK)
        } else {
          complete(StatusCodes.ServiceUnavailable)
        }
      }
    }
  }

  @Path("/health")
  @GET
  @Operation(
    summary = "Get platform health information",
    description = "Retrieves the health status information of the Case Engine",
    tags = Array("platform"),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Platform health report", content = Array(new Content(schema = new Schema(implementation = classOf[Object])))),
      new ApiResponse(responseCode = "500", description = "Not able to perform the action")
    )
  )
  @Produces(Array("application/json"))
  def health = get {
    pathPrefix("health") {
      pathEndOrSingleSlash {
        completeJsonValue(CaseSystem.health.report)
      }
    }
  }

  @Path("/version")
  @GET
  @Operation(
    summary = "Get platform version",
    description = "Retrieves the version information of the platform",
    tags = Array("platform"),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Version information", content = Array(new Content(schema = new Schema(implementation = classOf[Object])))),
      new ApiResponse(responseCode = "500", description = "Not able to perform the action")
    )
  )
  @Produces(Array("application/json"))
  def version = get {
    path("version") {
      completeJsonValue(CaseSystem.version.json)
    }
  }
}
