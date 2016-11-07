package org.broadinstitute.mdreport
import org.broadinstitute.MD.rest.MetricsQuery
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType
import org.broadinstitute.MD.types.metrics.{Metrics => _, _}
import org.broadinstitute.mdreport.ReporterTraits._

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
/**
  * Created by amr on 10/26/2016.
  */
object Reporters {

  class SmartSeqReporter(config: Config) extends Metrics with Samples with Query{
    val setId = config.setId
    val setVersion = config.version
    val sampleList = config.sampleList
    val metrics: List[MetricsType] = List(
      MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics,
      MetricsType.PicardMeanQualByCycle,
      MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats,
      MetricsType.RnaSeqQcStats
    )
    val sampleRefs = makeSampleRefs(setId = setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
    val sampleRequests = makeSampleRequests(sr = sampleRefs,
      metrics = metrics,
      sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
    val mq = makeMetricsQuery(sampleRequests)
    var port = 9100
    if (config.test) port = 9101
    val path = s"http://btllims.broadinstitute.org:$port/MD/findMetrics"
    def run() = {
      doQuery()
      //TODO: Write returned object to some file
    }
  }
}
