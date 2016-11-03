import akka.actor.ActorSystem
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import org.scalatest.{FlatSpec, Matchers}
import org.broadinstitute.MD.rest.{MetricsQuery, SampleMetrics}
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import org.broadinstitute.MD.types.metrics.{Metrics, MetricsType, PicardInsertSizeMetrics, PicardMeanQualByCycle}
import org.broadinstitute.mdreport.Request
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
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
}
