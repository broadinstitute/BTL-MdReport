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

  class SmartSeqReporter(config: Config) extends Metrics with Samples {
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

    def makeSampleRefs(srefs: ListBuffer[SampleRef]): ListBuffer[SampleRef] = {
      @tailrec
      def refAccumulator(srefs: ListBuffer[SampleRef]): ListBuffer[SampleRef] = {
        if (sampleList.hasNext) {
          srefs += SampleRef(sampleID = sampleList.next(), setID = setId)
          refAccumulator(srefs)
        } else {
          srefs
        }
      }
      refAccumulator(srefs)
    }

    def makeSampleRequests(sr: Iterator[SampleRef],
                           sreqs: ListBuffer[SampleMetricsRequest]):ListBuffer[SampleMetricsRequest] = {
      @tailrec
      def reqAccumulator(sreqs: ListBuffer[SampleMetricsRequest]):ListBuffer[SampleMetricsRequest] = {
        if (sr.hasNext) {
          sreqs += SampleMetricsRequest(sampleRef = sr.next(), metrics = metrics)
          reqAccumulator(sreqs)
        } else {
          sreqs
        }
      }
      reqAccumulator(sreqs)
    }

    def run() = {
      val sampleRefs = makeSampleRefs(scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
      val sampleRequests = makeSampleRequests(sampleRefs,
        scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
      val mq = MetricsQuery(
        id = setId,
        version = setVersion,
        sampleRequests = sampleRequests.toList
      )
    }
  }
}
