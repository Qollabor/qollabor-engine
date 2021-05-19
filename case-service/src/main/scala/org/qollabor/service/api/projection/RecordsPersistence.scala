package org.qollabor.service.api.projection

import akka.Done
import org.qollabor.service.api.projection.record._

import scala.concurrent.Future

trait RecordsPersistence {
  def bulkUpdate(records: Seq[AnyRef]): Future[Done]

  def getUserRole(key: UserRoleKey): Future[Option[UserRoleRecord]]

  def getPlanItem(planItemId: String): Future[Option[PlanItemRecord]]

  def getCaseFile(caseInstanceId: String): Future[Option[CaseFileRecord]]

  def getCaseInstance(caseInstanceId: String): Future[Option[CaseRecord]]

  def getTask(taskId: String): Future[Option[TaskRecord]]
}
