package org.broadinstitute.mdreport
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.mdreport.ReporterTraits._

/**
  * Created by amr on 10/26/2016.
  */
object Reporters {

  class SmartSeqReporter() extends Identity with SampleList with Query {
    val id = ""
    val version: Option[Long] = Some(1.toLong)
    val sampleList = List()
    def run() = query(id, version, sampleList )
  }

}
