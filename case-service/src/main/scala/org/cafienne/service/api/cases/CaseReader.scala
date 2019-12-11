package org.cafienne.service.api.cases

import com.typesafe.scalalogging.LazyLogging
import org.cafienne.cmmn.akka.command.response.CaseLastModified
import org.cafienne.cmmn.akka.event.CaseModified
import org.cafienne.service.api.projection.LastModifiedRegistration

import scala.concurrent.{ExecutionContext, Future}

trait CaseReader extends LazyLogging {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def handleSyncedQuery[A](query: () => Future[A], clm: Option[String]): Future[A] = {
    clm match {
      case Some(s) =>
        // Now go to the writer and ask it to wait for the clm for this case instance id...
        val promise = CaseReader.lastModifiedRegistration.waitFor(new CaseLastModified(s))
        promise.future.flatMap(_ => query())
      case None => // Nothing to do, just continue
        query()
    }
  }
}

object CaseReader {
  val lastModifiedRegistration: LastModifiedRegistration = new LastModifiedRegistration("Cases")

  def inform(caseModified: CaseModified) = {
    lastModifiedRegistration.handle(caseModified)
  }
}
