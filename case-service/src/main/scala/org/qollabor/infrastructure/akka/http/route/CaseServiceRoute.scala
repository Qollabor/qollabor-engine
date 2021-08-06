package org.qollabor.infrastructure.akka.http.route

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import ch.megard.akka.http.cors.scaladsl.model.HttpHeaderRange
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.typesafe.scalalogging.LazyLogging
import org.qollabor.akka.actor.serialization.json.Value
import org.qollabor.infrastructure.json.QollaborJson
import org.qollabor.service.api

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Base class for Case Service APIs. All cors enabled
  */
trait CaseServiceRoute extends LazyLogging {

  import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

  val corsSettings = CorsSettings.defaultSettings
    .withAllowedHeaders(HttpHeaderRange("Authorization", "Content-Type", "X-Requested-With", api.CASE_LAST_MODIFIED, api.TENANT_LAST_MODIFIED, "accept", "origin"))
    .withAllowedMethods(Seq(GET, POST, HEAD, OPTIONS, PUT, DELETE))
    .withMaxAge(Some(200L)
    )

  val rejectionHandler = corsRejectionHandler withFallback requestServiceRejectionHandler
  val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)

  val route: Route = handleErrors {
    extractExecutionContext { implicit executor =>
      cors(corsSettings) {
        handleErrors { req => {
          //          println("Asking "+req.request.uri)
          routes(req).map(resp => {
            //            println("Responding to "+req.request.uri+": "+resp)
            //            println(""+resp)
            resp
          })
        }
        }
      }
    }
  }

  def requestServiceRejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case MalformedRequestContentRejection(errorMessage, e) =>
          extractUri { uri =>
            logger.debug("Exception of type " + e.getClass.getName + " occured in handling HTTP request " + uri.path + " - " + errorMessage)
            complete(StatusCodes.BadRequest, "The request content was malformed:\n" + errorMessage)
          }
      }
      .handle {
        case AuthorizationFailedRejection ⇒ complete(StatusCodes.Forbidden)
      }
      .result()

  def exceptionHandler = ExceptionHandler {
    case exception: Throwable => defaultExceptionHandler(exception)
  }

  def defaultExceptionHandler(t: Throwable): Route = {
    t match {
      case h: UnhealthyCaseSystem => complete(HttpResponse(StatusCodes.ServiceUnavailable, entity = h.getLocalizedMessage))
      case _ => extractUri { uri =>
        extractMethod { method =>
          // Depending on debug logging - either print full exception or only headline
          if (logger.underlying.isDebugEnabled()) {
            logger.debug(s"Bumped into an exception in ${this.getClass().getSimpleName} on ${method.name} $uri", t)
          } else if (logger.underlying.isInfoEnabled()) {
            logger.info(s"Bumped into an exception in ${this.getClass().getSimpleName} on ${method.name} $uri:\n" + t)
          } else {
            logger.warn(s"Bumped into ${t.getClass.getName} in ${this.getClass().getSimpleName} on ${method.name} $uri - enable debug logging for stack trace")
          }
          complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }
  }

  def completeQollaborJSONSeq(seq: Seq[QollaborJson]) = {
    completeJsonValue(Value.convert(seq.map(element => element.toValue)))
  }

  def completeJsonValue(v: Value[_]) = {
    complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, v.toString))
  }

  def routes: Route

  /**
    * Override this method in your route to expose the Swagger API classes
    *
    * @return
    */
  def apiClasses(): Seq[Class[_]] = {
    Seq()
  }
}
