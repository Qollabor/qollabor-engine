/*
 * Copyright 2014 - 2019 Qollabor B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.qollabor.service.api.tasks

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import akka.http.scaladsl.server.Route
import org.qollabor.akka.actor.identity.{PlatformUser, TenantUser}
import org.qollabor.humantask.akka.command.WorkflowCommand
import org.qollabor.infrastructure.akka.http.route.{CommandRoute, QueryRoute}
import org.qollabor.service.api.cases.CaseReader
import org.qollabor.service.api.projection.TaskSearchFailure
import org.qollabor.service.api.projection.query.TaskQueries

import scala.util.{Failure, Success}

trait TaskRoute extends CommandRoute with QueryRoute {
  val taskQueries: TaskQueries

  override val lastModifiedRegistration = CaseReader.lastModifiedRegistration

  def askTaskWithMember(platformUser: PlatformUser, taskId: String, userId: String, createTaskCommand: CreateTaskCommandWithMember): Route = {
    onComplete(taskQueries.authorizeTaskAccessAndReturnCaseAndTenantId(taskId, platformUser)) {
      case Success((caseInstanceId, tenant)) => {
        onComplete(userCache.getUsers(Seq(userId), tenant)) {
          case Success(tenantUsers) => {
            if (tenantUsers.isEmpty) {
              // Not found, hence not a valid user (it can be also because the user account is not enabled)
              complete(StatusCodes.NotFound, s"Cannot find an active user '$userId' in tenant '$tenant'")
            } else if (tenantUsers.size > 1) {
              logger.error(s"Found ${tenantUsers.size} users matching userId '$userId' in tenant '$tenant'. The query should only result in one user only.")
              complete(StatusCodes.InternalServerError, s"An internal error happened while retrieving user information on user '$userId'")
            } else {
              val member = tenantUsers(0)
              askModelActor(createTaskCommand.apply(caseInstanceId, platformUser.getTenantUser(tenant), member))
            }
          }
          case Failure(t: Throwable) => {
            logger.warn(s"An error happened while retrieving user information on user '$userId' in tenant '$tenant'", t)
            complete(StatusCodes.InternalServerError, s"An internal error happened while retrieving user information on user '$userId'")
          }
        }
      }
      case Failure(error) => {
        error match {
          case t: TaskSearchFailure => complete(StatusCodes.NotFound, t.getLocalizedMessage)
          case _ => throw error
        }
      }
    }
  }

  def askTask(platformUser: PlatformUser, taskId: String, createTaskCommand: CreateTaskCommand): Route = {
    onComplete(taskQueries.authorizeTaskAccessAndReturnCaseAndTenantId(taskId, platformUser)) {
      case Success((caseInstanceId, tenant)) => askModelActor(createTaskCommand.apply(caseInstanceId, platformUser.getTenantUser(tenant)))
      case Failure(error) => {
        error match {
          case t: TaskSearchFailure => complete(StatusCodes.NotFound, t.getLocalizedMessage)
          case _ => throw error
        }
      }
    }
  }

  trait CreateTaskCommandWithMember {
    def apply(caseInstanceId: String, user: TenantUser, member: TenantUser): WorkflowCommand
  }

  trait CreateTaskCommand {
    def apply(caseInstanceId: String, user: TenantUser): WorkflowCommand
  }
}
