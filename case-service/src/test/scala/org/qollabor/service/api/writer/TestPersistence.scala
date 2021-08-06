package org.qollabor.service.api.writer

import java.time.Instant

import akka.Done
import org.qollabor.service.api.projection.RecordsPersistence
import org.qollabor.service.api.projection.record._

import scala.concurrent.Future

class TestPersistence() extends RecordsPersistence {
  var records: Seq[AnyRef] = Seq()

  override def bulkUpdate(objs: Seq[AnyRef]): Future[Done] = {
    records = records ++ objs
    Future.successful(Done)
  }

  override def getUserRole(key: UserRoleKey): Future[Option[UserRoleRecord]] = Future.successful(None)
  override def getPlanItem(planItemId: String): Future[Option[PlanItemRecord]] = Future.successful(None)
  override def getTask(taskId: String): Future[Option[TaskRecord]] = Future.successful(Some(TaskRecord(id = "1", caseInstanceId = "1", tenant = "tenant", createdOn = Instant.now, lastModified = Instant.now)))
  override def getCaseInstance(id: String): Future[Option[CaseRecord]] =  Future.successful(None)
  override def getCaseFile(caseInstanceId: String): Future[Option[CaseFileRecord]] = Future.successful(None)
}
