package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.rest.SampleMetrics
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.marshallers.Marshallers._
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType
import org.broadinstitute.MD.types.metrics.{Metrics => _, _}
import org.broadinstitute.mdreport.ReporterTraits._

import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.jsonutil._
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import org.broadinstitute.mdreport.MdReport.failureExit

import scala.collection.mutable
/**
  * Created by amr on 10/26/2016.
  */
object Reporters {
  private implicit lazy val system = ActorSystem()
  private implicit lazy val materializer = ActorMaterializer()
  private implicit lazy val ec = system.dispatcher
  class SmartSeqReporter(config: Config) extends Metrics with Samples with Requester with Output with MapMaker{
    val setId = config.setId
    val setVersion = config.version
    val sampleList = config.sampleList
    val delimiter = "\t"
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
    val path = s"http://btllims.broadinstitute.org:$port/MD/metricsQuery"
    val smartseqMap: mutable.LinkedHashMap[String, Any] = mutable.LinkedHashMap(
      "sampleName" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.totalReads" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.meanReadLength" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pfReads" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctPfReads" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pfReadsAligned" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctPfReadsAligned" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.readsAlignedInPairs" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctReadsAlignedInPairs" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctAdapter" -> None,
      "PicardMeanQualByCycle.r1MeanQual" -> None,
      "PicardMeanQualByCycle.r2MeanQual" -> None,
      "PicardInsertSizeMetrics.pairOrientation" -> None,
      "PicardInsertSizeMetrics.meanInsertSize" -> None,
      "PicardInsertSizeMetrics.medianInsertSize" -> None,
      "PicardInsertSizeMetrics.standardDeviation" -> None,
      "PicardReadGcMetrics.meanGcContent" -> None,
      "ErccStats.totalErccReads" -> None,
      "ErccStats.fractionErccReads" -> None,
      "ErccStats.fractionGenomeReferenceReads" -> None,
      "ErccStats.totalUnalignedReads" -> None,
      "ErccStats.fractionUnalignedReads" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.fragmentLengthMean" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.chimericPairs" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.readLength" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.estimatedLibrarySize" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.fragmentLengthStdDev" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.expressionProfilingEfficiency" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.unpairedReads" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.baseMismatchRate" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.transcriptsDetected" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.totalPurityFilteredReadsSequenced" -> None,
      "RnaSeqQcStats.readMetrics.ReadMetrics.failedVendorQCCheck" -> None,
      "RnaSeqQcStats.covMetrics.CovMetrics.meanPerBaseCov" -> None,
      "RnaSeqQcStats.covMetrics.CovMetrics.meanCV" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mapped" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappedPairs" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.alternativeAlignments" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.uniqueRateofMapped" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappingRate" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappedUnique" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.end1MappingRate" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.end2MappingRate" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappedUniqueRateofTotal" -> None,
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.duplicationRateOfMapped" -> None,
      "RnaSeqQcStats.gapMetrics.GapMetrics.cumulGapLength" -> None,
      "RnaSeqQcStats.gapMetrics.GapMetrics.gapPct" -> None,
      "RnaSeqQcStats.gapMetrics.GapMetrics.numGaps" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.intronicRate" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.rRNA" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.genesDetected" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.rRNArate" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.exonicRate" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.intragenicRate" -> None,
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.intergenicRate" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end1PctSense" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end1Antisense" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.noCovered5Prime" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end2MismatchRate" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end2Antisense" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end2PctSense" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end2Sense" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end1MismatchRate" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.end1Sense" -> None,
      "RnaSeqQcStats.endMetrics.EndMetrics.fivePrimeNorm" -> None,
      "RnaSeqQcStats.Notes" -> None
    )
    def run() = {
      val sampleRefs = makeSampleRefs(setId = setId,
        srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
      val sampleRequests = makeSampleRequests(sr = sampleRefs,
        metrics = metrics,
        sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
      val mq = makeMetricsQuery(sampleRequests)
      val query = doQuery(mq)
      val result = query.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
      val metricsList = Await.result(result, 5 seconds)
      val mapsList = fillMap(smartseqMap, metricsList)
    }
  }
//  class LegacyReporter(config: Config) extends Requester with LegacyExtractor with Output{
//    var port = 9100
//    if (config.test) port = 9101
//    val path = s"http://btllims.broadinstitute.org:$port/MD/find/metrics"
//    val setId = config.setId
//    val setVersion = config.version
//    def run() = {
//      val request = doFind(setId, setVersion)
//      //val result = unmarshaller[BaseJson](request)
//      val metrics = Await.result(result, 5 seconds)
//      val metrics_list = legacy_extract(metrics)
//      setVersion match {
//        case Some(v) => legacy_write(metrics_list, config.outDir, setId, v)
//          System.exit(0)
//        case None => failureExit("Metrics version not specified.")
//      }
//
//    }
//  }
}
