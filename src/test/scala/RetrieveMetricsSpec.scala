/**
  * Created by Amr on 10/21/2016.
  */


import org.broadinstitute.mdreport.RetrieveMetrics
import org.scalatest.{FlatSpec, Matchers}

class RetrieveMetricsSpec extends FlatSpec with Matchers {

  "RetrieveMetrics" should "mreturn a list" in {
    //val rm = new RetrieveMetrics("SSF-1751", 1477521489932L)
    val rm = new RetrieveMetrics("parsomatic_unit_test", 1)
    val metrics = rm.retrieve()
    for (metric <- metrics) println(metric + "\n")
  }
}
