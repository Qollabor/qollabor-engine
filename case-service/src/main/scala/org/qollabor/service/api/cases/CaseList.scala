package org.qollabor.service.api.cases

import org.qollabor.akka.actor.serialization.json.ValueMap
import org.qollabor.infrastructure.json.QollaborJson

final case class CaseList(caseName: String = "",
                          totalInstances: Long = 0,
                          numActive:Long = 0,
                          numCompleted:Long = 0,
                          numTerminated:Long = 0,
                          numSuspended:Long = 0,
                          numFailed:Long = 0,
                          numClosed:Long = 0,
                          numWithFailures: Long = 0) extends QollaborJson {

  override def toValue = {
    val v = new ValueMap
    v.putRaw("caseName", caseName)
    v.putRaw("totalInstances", totalInstances)

    v.putRaw("numActive", numActive)
    v.putRaw("numCompleted", numCompleted)
    v.putRaw("numTerminated", numTerminated)

    v.putRaw("numSuspended", numSuspended)
    v.putRaw("numFailed", numFailed)
    v.putRaw("numClosed", numClosed)
    v.putRaw("numWithFailures", numWithFailures)
    v
  }
}
