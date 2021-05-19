package org.qollabor.infrastructure.akka.http.route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.{complete, onComplete, respondWithHeader}
import akka.http.scaladsl.server.{Directive0, Route}
import org.qollabor.akka.actor.CaseSystem
import org.qollabor.akka.actor.command.ModelCommand
import org.qollabor.akka.actor.command.response.{CommandFailure, EngineChokedFailure, ModelResponse, SecurityFailure}
import org.qollabor.cmmn.akka.command.response.CaseResponse
import org.qollabor.humantask.akka.command.response.{HumanTaskResponse, HumanTaskValidationResponse}
import org.qollabor.infrastructure.akka.http.ResponseMarshallers._
import org.qollabor.infrastructure.akka.http.ValueMarshallers._
import org.qollabor.service.{Main, api}
import org.qollabor.tenant.akka.command.response.{TenantOwnersResponse, TenantResponse}

import scala.util.{Failure, Success}

trait CommandRoute extends AuthenticatedRoute {

  import akka.pattern.ask
  implicit val timeout = Main.caseSystemTimeout

  def askModelActor(command: ModelCommand[_]): Route = {
    onComplete(CaseSystem.router ? command) {
      case Success(value) =>
        value match {
          case s: SecurityFailure => complete(StatusCodes.Unauthorized, s.exception.getMessage)
          case e: EngineChokedFailure => complete(StatusCodes.InternalServerError, "An error happened in the server; check the server logs for more information")
          case e: CommandFailure => complete(StatusCodes.BadRequest, e.exception.getMessage)
          case tenantOwners: TenantOwnersResponse => complete(StatusCodes.OK, tenantOwners)
          case value: TenantResponse => writeLastModifiedHeader(value, api.TENANT_LAST_MODIFIED) {
            complete(StatusCodes.NoContent)
          }
          case value: HumanTaskValidationResponse =>
            writeLastModifiedHeader(value) {
              complete(StatusCodes.Accepted, value.value())
            }
          case value: HumanTaskResponse =>
            writeLastModifiedHeader(value) {
              complete(StatusCodes.Accepted, value)
            }
          case value: CaseResponse =>
            writeLastModifiedHeader(value) {
              complete(StatusCodes.OK, value)
            }
        }
      case Failure(e) => complete(StatusCodes.InternalServerError, e.getMessage)
    }
  }

  def writeLastModifiedHeader(response: ModelResponse, headerName: String = api.CASE_LAST_MODIFIED): Directive0 = {
    respondWithHeader(RawHeader(headerName, response.lastModifiedContent.toString))
  }
}
