package org.qollabor.infrastructure.akka.http.route

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, onComplete, optionalHeaderValueByName}
import akka.http.scaladsl.server.{Directive1, Route}
import org.qollabor.akka.actor.command.exception.AuthorizationException
import org.qollabor.akka.actor.command.response.ActorLastModified
import org.qollabor.akka.actor.serialization.json.Value
import org.qollabor.infrastructure.json.QollaborJson
import org.qollabor.service.api
import org.qollabor.service.api.cases.CaseDefinitionDocument
import org.qollabor.service.api.projection.{LastModifiedRegistration, SearchFailure}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait QueryRoute extends AuthenticatedRoute {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val lastModifiedRegistration: LastModifiedRegistration

  val lastModifiedHeaderName: String = api.CASE_LAST_MODIFIED

  def readLastModifiedHeader(): Directive1[Option[String]] = {
    optionalHeaderValueByName(lastModifiedHeaderName)
  }

  def runXMLQuery(future: => Future[CaseDefinitionDocument]): Route = {
    readLastModifiedHeader() { caseLastModified =>
      handleXMLResult(handleSyncedQuery(() => future, caseLastModified))
    }
  }

  def handleXMLResult(future: => Future[CaseDefinitionDocument]): Route = {
    onComplete(future) {
      case Success(value) => complete(StatusCodes.OK, HttpEntity(ContentTypes.`text/xml(UTF-8)`, value.xml))
      case Failure(t) => handleFailure(t)
    }
  }

  def runQuery(future: => Future[QollaborJson]): Route = {
    readLastModifiedHeader() { caseLastModified =>
      handleQueryResult(handleSyncedQuery(() => future, caseLastModified))
    }
  }

  def runListQuery(future: => Future[Seq[QollaborJson]]): Route = {
    readLastModifiedHeader() { caseLastModified =>
      handleQueryResultList(handleSyncedQuery(() => future, caseLastModified))
    }
  }

  def handleQueryResult(future: => Future[QollaborJson]): Route = {
    onComplete(future) {
      case Success(value) => completeJsonValue(value.toValue)
      case Failure(t) => handleFailure(t)
    }
  }

  def handleQueryResultList(future: => Future[Seq[QollaborJson]]): Route = {
    onComplete(future) {
      case Success(value) => completeJsonValue(Value.convert(value.seq.map(v => v.toValue)))
      case Failure(t) => handleFailure(t)
    }
  }

  def handleFailure(t: Throwable): Route = {
    t match {
      case notFound: SearchFailure => complete(StatusCodes.NotFound, notFound.getLocalizedMessage)
      case notAllowed: AuthorizationException => complete(StatusCodes.Unauthorized, notAllowed.getMessage)
      case notAllowed: SecurityException => complete(StatusCodes.Unauthorized, notAllowed.getMessage)
      case error => {
        logger.whenDebugEnabled(() => logger.debug("Failure while handling query", error))
        complete(StatusCodes.InternalServerError)
      }
    }
  }

  def handleSyncedQuery[A](query: () => Future[A], clm: Option[String]): Future[A] = {
    clm match {
      case Some(s) =>
        // Now go to the writer and ask it to wait for the clm for this case instance id...
        val promise = lastModifiedRegistration.waitFor(new ActorLastModified(s))
        promise.future.flatMap(_ => query())
      case None => // Nothing to do, just continue
        query()
    }
  }
}
