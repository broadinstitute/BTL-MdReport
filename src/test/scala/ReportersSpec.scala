import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import org.scalatest.{FlatSpec, Matchers}
import org.broadinstitute.MD.rest.{MetricsQuery, SampleMetrics}
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import org.broadinstitute.MD.types.metrics.{Metrics, MetricsType, PicardInsertSizeMetrics, PicardMeanQualByCycle}
import org.broadinstitute.mdreport.Request

import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.mdreport.RetrieveMetrics
import akka.http.scaladsl.unmarshalling.Unmarshal
/**
  * Created by amr on 11/2/2016.
  */

class ReportersSpec extends FlatSpec with Matchers{
  val sampleRef1 = SampleRef(
    sampleID = "SSF1859B12_A375_AkiYoda",
    setID = "SSF-1859"
  )
  val sampleRef2 = SampleRef(
    sampleID = "SSF1859A11_A375_AkiYoda",
    setID = "SSF-1859"
  )
  val mq = MetricsQuery(id = "SSF-1859", version = Some(7),
    sampleRequests = List(
      SampleMetricsRequest(
        sampleRef = sampleRef1,
        metrics = List(MetricsType.RnaSeqQcStats, MetricsType.ErccStats)),
      SampleMetricsRequest(
        sampleRef = sampleRef2,
        metrics = List(MetricsType.RnaSeqQcStats, MetricsType.ErccStats))
    )
  )
  val json = MetricsQuery.writeJson(mq)
  "" should "" in {
    val request = new Request().doRequest(
      path = "http://btllims.broadinstitute.org:9101/MD/metricsQuery",
      json = json
    )
    val response = request.flatMap(response => Unmarshal(response.entity).to[List[BaseJson]])
    val result = Await.result(response, 5 seconds)
    var metrics: List[String] = List()
    for (x <- result.toIterator) {
      x match {
        case m: Metrics => metrics = m.toCsv
        case _ =>
      }
    }

  }
}
