package org.qollabor.service.api.writer

import java.time.Instant

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.testkit.{TestKit, TestProbe}
import org.qollabor.cmmn.test.TestScript
import org.qollabor.identity.TestIdentityFactory
import org.qollabor.service.api.projection.cases.CaseProjectionsWriter
import org.qollabor.service.api.projection.record.{CaseRecord, PlanItemRecord}
import org.qollabor.service.db.querydb.QueryDB
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class PlanItemWriterTest
  extends TestKit(ActorSystem("testsystem", TestConfig.config))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with Eventually {

  //Ensure the database is setup completely (including the offset store)
  QueryDB.verifyConnectivity()

  private val storeEventsActor = system.actorOf(Props(classOf[CreateEventsInStoreActor]), "storeevents-actor")
  private val tp = TestProbe()

  implicit val logger: LoggingAdapter = Logging(system, getClass)

  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(2, Seconds)),
    interval = scaled(Span(5, Millis)))

  private def sendEvent(evt: Any) = {
    within(5 seconds) {
      tp.send(storeEventsActor, evt)
      tp.expectMsg(evt)
    }
  }

  val persistence = new TestPersistence()

  val cpw = new CaseProjectionsWriter(persistence, NoOffsetStorage)
  cpw.start()


  val caseInstanceId = "c140aae8_dd10_4ece_8fb1_5f7a199e49e7"
  val user = TestIdentityFactory.createTenantUser("test")
  val caseDefinition = TestScript.getCaseDefinition("helloworld.xml")

  val eventFactory = new EventFactory(caseInstanceId, caseDefinition, user)
  val ivm = Instant.now

  val caseDefinitionApplied = eventFactory.createCaseDefinitionApplied()
  val caseModifiedEvent = eventFactory.createCaseModified(ivm)
  val planItemCreated = eventFactory.createPlanItemCreated("1", "CasePlan", "HelloWorld", "", ivm)

  "CaseProjectionsWriter" must {
    "add and update plan items" in {

      sendEvent(caseDefinitionApplied)
      sendEvent(planItemCreated)
      sendEvent(caseModifiedEvent)

      Thread.sleep(1000)
      eventually {
        persistence.records.length shouldBe 8
        assert(persistence.records.exists(x => x.isInstanceOf[CaseRecord]))
        assert(persistence.records.exists(x => x.isInstanceOf[PlanItemRecord]))
      }
    }
  }
}
