package org.broadinstitute.mdreport
import com.norbitltd.spoiwo.model.{Row, Sheet}
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import org.broadinstitute.MD.rest.MetricsQuery
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest

/**
  * Created by amr on 10/26/2016.
  */
object ReporterTraits {

  trait Identity {
    val id: String
    val version: Long
  }

  trait SampleList {
    val samples = List[SampleMetricsRequest]
  }

  trait Query {
    def query = MetricsQuery
  }
}
