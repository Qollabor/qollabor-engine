package org.qollabor.service.api.writer

import java.time.Instant

import akka.actor.{ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.testkit.{TestKit, TestProbe}
import org.qollabor.akka.actor.serialization.json.ValueMap
import org.qollabor.cmmn.instance.casefile.CaseFileItemTransition
import org.qollabor.cmmn.test.TestScript
import org.qollabor.identity.TestIdentityFactory
import org.qollabor.service.api.projection.cases.CaseProjectionsWriter
import org.qollabor.service.api.projection.record.{CaseFileRecord, CaseRecord}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class CaseFileWriterTest
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

  val ivm = Instant.now()
  val caseDefinitionApplied = eventFactory.createCaseDefinitionApplied()
  val path = "Greeting"
//  val jsonValue = new ValueMap("Greeting", new ValueMap("Message", "hi there", "From", "admin"))
  val jsonValue = new ValueMap("Message", "hi there", "From", "admin")
  val caseFileEvent = eventFactory.createCaseFileEvent(path, jsonValue, CaseFileItemTransition.Create)
  val caseModifiedEvent = eventFactory.createCaseModified(ivm)

//  def getJSON(value: String): ValueMap =
//    if (value == "" || value == null) new ValueMap
//    else JSONReader.parse(value)

  "CaseProjectionsWriter" must {
    "add and update a case file" in {

      sendEvent(caseDefinitionApplied)
      sendEvent(caseFileEvent)
      sendEvent(caseModifiedEvent)

      val expectedCaseFileContent = """{
                             |  "Greeting" : {
                             |    "Message" : "hi there",
                             |    "From" : "admin"
                             |  }
                             |}""".stripMargin
      Thread.sleep(2000)

      eventually {
        persistence.records.length shouldBe 6
        persistence.records.head shouldBe a[CaseRecord]
        persistence.records(2) match {
          case cs: CaseFileRecord =>
            cs.data should be(expectedCaseFileContent)
          case other => assert(false, "CaseFile object expected, found " + other.getClass.getName)
        }
      }
    }
  }
}
