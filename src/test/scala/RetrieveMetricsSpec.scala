/**
  * Created by Amr on 10/21/2016.
  */


import org.broadinstitute.mdreport.RetrieveMetrics
import org.scalatest.{FlatSpec, Matchers}

class RetrieveMetricsSpec extends FlatSpec with Matchers {

  "RetrieveMetrics" should "return a list" in {
    val rm = new RetrieveMetrics("EntryCreatorSystemTest1", 1)
    val metrics = rm.retrieve()
    for (metric <- metrics) println(metric + "\n")
  }
}
