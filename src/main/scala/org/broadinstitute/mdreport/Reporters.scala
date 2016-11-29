package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.rest.SampleMetrics
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType
import org.broadinstitute.MD.types.metrics.{Metrics => _, _}
import org.broadinstitute.mdreport.ReporterTraits._
import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.jsonutil._

/**
  * Created by amr on 10/26/2016.
  */
object Reporters {
  private implicit lazy val system = ActorSystem()
  private implicit lazy val materializer = ActorMaterializer()
  private implicit lazy val ec = system.dispatcher
  class SmartSeqReporter(config: Config) extends Metrics with Samples with Query with Output {
    val setId = config.setId
    val setVersion = config.version
    val sampleList = config.sampleList
    val bookName = config.outDir + "/" + config.setId + ".xslx"
    val metrics: List[MetricsType] = List(
      MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics,
      MetricsType.PicardMeanQualByCycle,
      MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats,
      MetricsType.RnaSeqQcStats
    )
    var port = 9100
    if (config.test) port = 9101
    //This path below needs to be corrected, Thaniel can help.
    val path = s"http://btllims.broadinstitute.org:$port/MD/metricsQuery"

    def run() = {
      val sampleRefs = makeSampleRefs(setId = setId,
        srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
      val sampleRequests = makeSampleRequests(sr = sampleRefs,
        metrics = metrics,
        sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
      val mq = makeMetricsQuery(sampleRequests)
      val query = doQuery(mq)
      //val response = query.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
      //println(response)
      //val response = doQuery(mq)
      val result = Await.result(query, 5 seconds)
      println(result.entity)
    }
  }
}
