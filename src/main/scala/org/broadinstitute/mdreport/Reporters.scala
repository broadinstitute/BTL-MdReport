package org.broadinstitute.mdreport
import org.broadinstitute.MD.rest.MetricsQuery
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.metrics.{Metrics => _, _}
import org.broadinstitute.mdreport.ReporterTraits._
import scala.annotation.tailrec

/**
  * Created by amr on 10/26/2016.
  */
object Reporters {

  class SmartSeqReporter(config: Config) extends Metrics with Samples {
    val setId = config.setId
    val setVersion = config.version
    val metrics = List(PicardAlignmentSummaryAnalysis,
      PicardInsertSizeMetrics,
      PicardMeanQualByCycle,
      PicardReadGcMetrics,
      ErccStats,
      RnaSeqQcStats)
    // This list should contain a user-supplied list of samples as samplerefs
    val sampleList = config.sampleList.split(",")
    val sampleRefs = List("")
    //This list should be constructed using sampleRefs list + metrics list.
    val sampleRequests = List("")
    def run = {
      def makeSampleRequests(lb: scala.collection.mutable.ListBuffer[SampleMetricsRequest]) = {
        def accumulator(lb: scala.collection.mutable.ListBuffer[SampleMetricsRequest]) = {

        }
      }

      for (sample <- sampleList) {
        makeSampleRef(setId, sample)
      }
    }
//    val mq = MetricsQuery(id = setId,
//      version = Some(setVersion),
//      sampleRequests = sampleRequests)
  }
}
