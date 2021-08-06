package org.qollabor.service.api.writer

import java.time.Instant

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.testkit.{TestKit, TestProbe}
import org.qollabor.cmmn.instance.{State, Transition}
import org.qollabor.cmmn.test.TestScript
import org.qollabor.identity.TestIdentityFactory
import org.qollabor.service.api.projection.cases.CaseProjectionsWriter
import org.qollabor.service.api.projection.record.CaseRecord
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._

class CaseTaskWriterTest
  extends TestKit(ActorSystem("testsystem", TestConfig.config))
    with AnyWordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with Eventually {

  private val storeEventsActor = system.actorOf(Props(classOf[CreateEventsInStoreActor]), "storeevents-actor")
  private val tp = TestProbe()

  implicit val logger: LoggingAdapter = Logging(system, getClass)

  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(2, Seconds)),
    interval = scaled(Span(5, Millis)))

  private def sendEvent(evt: Any) = {
    within(10 seconds) {
      tp.send(storeEventsActor, evt)
      tp.expectMsg(evt)
    }
  }

  val persistence = new TestPersistence()

  val cpw = new CaseProjectionsWriter(persistence, NoOffsetStorage)
  cpw.start()


  val caseInstanceId = "9fc49257_7d33_41cb_b28a_75e665ee3b2c"
  val user = TestIdentityFactory.createTenantUser("test")
  val caseDefinition = TestScript.getCaseDefinition("helloworld.xml")

  val eventFactory = new EventFactory(caseInstanceId, caseDefinition, user)
  val ivm = Instant.now

  val caseDefinitionApplied = eventFactory.createCaseDefinitionApplied()
  val caseModifiedEvent = eventFactory.createCaseModified(ivm)
  val planItemTransitioned = eventFactory.createPlanItemTransitioned("1", "HumanTask", State.Terminated, State.Active, Transition.Terminate, ivm)

  "CaseProjectionsWriter" must {
    "add and update tasks" in {

      sendEvent(caseDefinitionApplied)
      sendEvent(planItemTransitioned)
      sendEvent(caseModifiedEvent)

      Thread.sleep(2000)
      eventually {
        persistence.records.length shouldBe 7
        persistence.records.exists(x => x.isInstanceOf[CaseRecord]) shouldBe (true)
      }
    }
  }
}
