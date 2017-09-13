package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import org.broadinstitute.MD.rest.{SampleMetricsRequest, SampleMetrics, MetricSamples, MetricsSamplesQuery}
import org.broadinstitute.MD.types.marshallers.Marshallers._
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType
import org.broadinstitute.MD.types.metrics.{Metrics => _, _}
import org.broadinstitute.mdreport.ReporterTraits._
import org.broadinstitute.mdreport.MdReport.failureExit
import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by amr on 10/26/2016.
  */
object Reporters {
  private val logger = Logger("Reporter")
  private implicit lazy val system = ActorSystem()
  private implicit lazy val materializer = ActorMaterializer()
  private implicit lazy val ec = system.dispatcher
  // rootPath for production
  private val rootPath = "http://btllims.broadinstitute.org"
  // rootPath for my work localhost testing.
  // private val rootPath = "http://GP3C5-33B.broadinstitute.org"
  // rootpath for home localhost testing
  //private val rootPath = "http://osiris-pc"
  def getSamples(setId: String, version: Option[Long], server: String): List[String] = {
    val path = s"$server/metricsSamplesQuery"
    val mq = MetricsSamplesQuery(setId, version)
    val json = MetricsSamplesQuery.writeJson(mq)
    val future = Request.doRequest(path, json)
    val result = future.flatMap(response => Unmarshal(response.entity).to[MetricSamples])
    val awaited = Await.result(result, 60 seconds)
    val sampleList = awaited.sampleRequests.toIterator
    def makeSampleList(lb: mutable.ListBuffer[String]): List[String] = {
      @tailrec
      def sampleAcc(lb: mutable.ListBuffer[String]): mutable.ListBuffer[String] = {
        if (sampleList.hasNext) {
          lb += sampleList.next().sampleRef.sampleID
          sampleAcc(lb)
        } else {
          lb
        }
      }
      sampleAcc(lb).toList
    }
    makeSampleList(mutable.ListBuffer[String]())
  }

  class SmartSeqReporter(config: Config) extends Metrics with Samples with Requester with Output with MapMaker with Log{
    val setId: String = config.setId.get
    val setVersion: Option[Long] = config.version
    var port = 9100
    if (config.test) port = 9101
    val server = s"$rootPath:$port/MD"
    val path = s"$server/metricsQuery"
    val sampleList: List[String] = config.sampleList.getOrElse(getSamples(setId, setVersion, server))
    val delimiter = "\t"
    val outDir: String = config.outDir
    val metrics: List[MetricsType] = List(
      MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics,
      MetricsType.PicardMeanQualByCycle,
      MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats,
      MetricsType.RnaSeqQcStats,
      MetricsType.DemultiplexedStats,
      MetricsType.PicardEstimateLibraryComplexity,
      MetricsType.SampleSheet
    )
    val smartseqMap: mutable.LinkedHashMap[String, Any] = mutable.LinkedHashMap(
      "sampleName" -> None,
      "SampleSheet.indexBarcode1" -> None,
      "SampleSheet.indexBarcode2" -> None,
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.totalReads" -> None,
      "DemultiplexedStats.pctOfMultiplexedReads" -> None,
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
      "PicardEstimateLibraryComplexity.percentDuplication" -> None,
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
    logInit(logger, "SmartSeqReporter")
    def run(): Unit = {
      logger.info("Creating sampleRefs.")
      val sampleRefs = makeSampleRefs(setId = setId,
        srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
      logger.info("Creating sampleRequests.")
      val sampleRequests = makeSampleRequests(sr = sampleRefs,
        metrics = metrics,
        sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
      logger.info("Creating MetricsQuery.")
      val mq = makeMetricsQuery(sampleRequests)
      val query = doQuery(mq)
      // TODO: Theory is that by awaiting the result of query, issues where server does not response will be handled.
      // Would like to review this solution with someone before implementing it.
      val q_res = Await.result(query, 30 seconds)
      val metricsList = Unmarshal(q_res.entity).to[List[SampleMetrics]]
      //      val result = query.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
      //      val metricsList = Await.result(result, 5 seconds)
      logger.debug("test")
      logger.debug(s"Metrics received from database: ${metricsList.toString}")
      val mapsList = fillMap(smartseqMap, Await.result(metricsList, 5 seconds))
      logger.debug(s"Metrics map created.\n$mapsList")
      writeMaps(mapsList = mapsList, outDir = outDir, id = setId, v = setVersion.get)
    }
  }

  class CustomReporter(config: Config) extends Metrics with Samples with Requester with Output with MapMaker with Log{
    val setId: String = config.setId.get
    val setVersion: Option[Long] = config.version
    val delimiter: String = config.delimiter
    val outDir: String = config.outDir
    var port = 9100
    if (config.test) port = 9101
    val server = s"$rootPath:$port/MD"
    val path = s"$server/metricsQuery"
    val sampleList: List[String] = config.sampleList.getOrElse(getSamples(setId, setVersion, server))
    val customReport: CustomReport = parseRdf(config.rdfFile.get)
    val metrics: List[MetricsType.MetricsType] = customReport.contents.keys.toList
    logInit(logger, "CustomReporter")
    def parseRdf(f: String): CustomReport = {
      val delim = "\t"
      val out = scala.io.Source.fromFile(config.rdfFile.get).getLines.map(x =>
      {
        val iter = x.split(delim)
        Tuple2(MetricsType.withName(iter.head), iter.last.split(",").toList)
      }
      )
      CustomReport(ListMap(out.toSeq: _*))
    }
    def makeCustomMap(cr: CustomReport): mutable.LinkedHashMap[String, Any] = {
      val m: mutable.LinkedHashMap[String, Any] = mutable.LinkedHashMap("sampleName" -> None)
      for ((metricType, fieldList) <- cr.contents) {
        for (field <- fieldList) {
          m(s"$metricType.$field") = None
        }
      }
      m
    }
    def run(): Unit = {
      val sampleRefs = makeSampleRefs(setId = setId,
        srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
      val sampleRequests = makeSampleRequests(sr = sampleRefs,
        metrics = metrics,
        sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
      val mq = makeMetricsQuery(sampleRequests)
      val query = doQuery(mq)
      val result = query.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
      val customMap = makeCustomMap(customReport)
      val metricsList = Await.result(result, 5 seconds)
      val mapsList = fillMap(customMap, metricsList)
      writeMaps(mapsList = mapsList, outDir = outDir, id = setId, v = setVersion.get)
    }
  }

  class LegacyReporter(config: Config) extends Requester with LegacyExtractor with Output with Log{
    var port = 9100
    val delimiter = ","
    if (config.test) port = 9101
    val path = s"http://btllims.broadinstitute.org:$port/MD/find/metrics"
    val setId: String = config.setId.get
    val setVersion: Option[Long] = config.version
    //Passing empty lists to keep logInit happy for now. Eventually may be able to populate these for legacy reporter.
    val sampleList = List()
    val metrics = List()
    logInit(logger, "LegacyReporter")
    def run(): Unit = {
      val request = doFind(setId, setVersion)
      val result = request.flatMap(response => Unmarshal(response.entity).to[List[BaseJson]])
      val metrics = Await.result(result, 5 seconds)
      val metrics_list = legacyExtract(metrics)
      setVersion match {
        case Some(v) => legacyWrite(metrics_list, config.outDir, setId, v)
        case None => failureExit("Metrics version not specified.")
      }
    }
  }
}
