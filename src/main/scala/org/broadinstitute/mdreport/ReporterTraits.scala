package org.broadinstitute.mdreport
import com.norbitltd.spoiwo.model.{Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import org.broadinstitute.MD.rest.MetricsQuery
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.metrics.MetricsType
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType

/**
  * Created by amr on 10/26/2016.
  */
object ReporterTraits {

  trait Metrics {
    val setId: String
    val setVersion: Option[Long]
    val metrics: List[MetricsType]
  }

  trait Samples {
    val sampleList: List[String]
    val sampleRequests: List[SampleMetricsRequest]
    val sampleRefs: List[SampleRef]
    def makeSampleRef(setId: String, sampleId: String): SampleRef = {
      SampleRef(sampleID = sampleId, setID = setId)
    }
    def makeSampleRequest(sampleRef: SampleRef, metrics: List[MetricsType]): SampleMetricsRequest = {
      SampleMetricsRequest(sampleRef, metrics)
    }
  }
}
