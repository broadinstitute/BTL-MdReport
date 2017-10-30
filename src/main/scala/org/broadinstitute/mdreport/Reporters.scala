package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import org.broadinstitute.MD.rest.{MetricSamples, MetricsSamplesQuery, SampleMetrics, SampleMetricsRequest}
import org.broadinstitute.MD.types.marshallers.Marshallers._
import org.broadinstitute.MD.types.metrics.MetricsType.MetricsType
import org.broadinstitute.MD.types.metrics.{Metrics => _, _}
import org.broadinstitute.mdreport.ReporterTraits._
import org.broadinstitute.mdreport.MdReport.failureExit
import scala.concurrent.duration._
import scala.concurrent.{Await, TimeoutException}
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.util.Try
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
    var retries = 4
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
      MetricsType.DemultiplexedMultipleStats,
      MetricsType.PicardEstimateLibraryComplexity,
      MetricsType.SampleSheet
    )
    val smartseqOrder = List(
      "sampleName",
      "SampleSheet.organism",
      "SampleSheet.indexBarcode1",
      "SampleSheet.indexBarcode2",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.totalReads",
      "DemultiplexedStats.pctOfMultiplexedReads",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.meanReadLength",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pfReads",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctPfReads",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pfReadsAligned",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctPfReadsAligned",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.readsAlignedInPairs",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctReadsAlignedInPairs",
      "PicardAlignmentSummaryAnalysis.PicardAlignmentSummaryMetrics.pctAdapter",
      "PicardMeanQualByCycle.r1MeanQual",
      "PicardMeanQualByCycle.r2MeanQual",
      "PicardInsertSizeMetrics.pairOrientation",
      "PicardInsertSizeMetrics.meanInsertSize",
      "PicardInsertSizeMetrics.medianInsertSize",
      "PicardInsertSizeMetrics.standardDeviation",
      "PicardReadGcMetrics.meanGcContent",
      "PicardEstimateLibraryComplexity.percentDuplication",
      "ErccStats.totalErccReads",
      "ErccStats.fractionErccReads",
      "ErccStats.fractionGenomeReferenceReads",
      "ErccStats.totalUnalignedReads",
      "ErccStats.fractionUnalignedReads",
      "RnaSeqQcStats.readMetrics.ReadMetrics.fragmentLengthMean",
      "RnaSeqQcStats.readMetrics.ReadMetrics.chimericPairs",
      "RnaSeqQcStats.readMetrics.ReadMetrics.readLength",
      "RnaSeqQcStats.readMetrics.ReadMetrics.estimatedLibrarySize",
      "RnaSeqQcStats.readMetrics.ReadMetrics.fragmentLengthStdDev",
      "RnaSeqQcStats.readMetrics.ReadMetrics.expressionProfilingEfficiency",
      "RnaSeqQcStats.readMetrics.ReadMetrics.unpairedReads",
      "RnaSeqQcStats.readMetrics.ReadMetrics.baseMismatchRate",
      "RnaSeqQcStats.readMetrics.ReadMetrics.transcriptsDetected",
      "RnaSeqQcStats.readMetrics.ReadMetrics.totalPurityFilteredReadsSequenced",
      "RnaSeqQcStats.readMetrics.ReadMetrics.failedVendorQCCheck",
      "RnaSeqQcStats.covMetrics.CovMetrics.meanPerBaseCov",
      "RnaSeqQcStats.covMetrics.CovMetrics.meanCV",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mapped",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappedPairs",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.alternativeAlignments",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.uniqueRateofMapped",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappingRate",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappedUnique",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.end1MappingRate",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.end2MappingRate",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.mappedUniqueRateofTotal",
      "RnaSeqQcStats.alignmentMetrics.AlignmentMetrics.duplicationRateOfMapped",
      "RnaSeqQcStats.gapMetrics.GapMetrics.cumulGapLength",
      "RnaSeqQcStats.gapMetrics.GapMetrics.gapPct",
      "RnaSeqQcStats.gapMetrics.GapMetrics.numGaps",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.intronicRate",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.rRNA",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.genesDetected",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.rRNArate",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.exonicRate",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.intragenicRate",
      "RnaSeqQcStats.annotationMetrics.AnnotationMetrics.intergenicRate",
      "RnaSeqQcStats.endMetrics.EndMetrics.end1PctSense",
      "RnaSeqQcStats.endMetrics.EndMetrics.end1Antisense",
      "RnaSeqQcStats.endMetrics.EndMetrics.noCovered5Prime",
      "RnaSeqQcStats.endMetrics.EndMetrics.end2MismatchRate",
      "RnaSeqQcStats.endMetrics.EndMetrics.end2Antisense",
      "RnaSeqQcStats.endMetrics.EndMetrics.end2PctSense",
      "RnaSeqQcStats.endMetrics.EndMetrics.end2Sense",
      "RnaSeqQcStats.endMetrics.EndMetrics.end1MismatchRate",
      "RnaSeqQcStats.endMetrics.EndMetrics.end1Sense",
      "RnaSeqQcStats.endMetrics.EndMetrics.fivePrimeNorm",
      "RnaSeqQcStats.Notes"
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
      query match {
        case Some(r) =>
          //val responseStr = Unmarshal(r.entity).to[String]
          val metricsList = Await.result(Unmarshal(r.entity).to[List[SampleMetrics]], 60 seconds)
          //val responseStr = Unmarshal(r.entity).to[String]
          logger.debug(s"Metrics received from database: ${metricsList.toString}")
          //val mapsList = fillMap(smartseqMap, metricsList)
          val mapsList = makeMap(smartseqOrder, metricsList)
          logger.debug(s"Metrics map created.\n$mapsList")
          writeMaps(mapsList = mapsList, outDir = outDir, id = setId, v = setVersion.get)
        case None => failureExit("Metrics not received from database.")
      }
    }
  }

  class CustomReporter(config: Config) extends Metrics with Samples with Requester with Output with MapMaker with Log{
    var retries = 4
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
      val customMap = makeCustomMap(customReport)

      val query = doQuery(mq)
      query match {
        case Some(r) =>
          val metricsList = Unmarshal(r.entity).to[List[SampleMetrics]]
          logger.debug(s"Metrics received from database: ${metricsList.toString}")
          val mapsList = fillMap(customMap, Await.result(metricsList, 60 seconds))
          logger.debug(s"Metrics map created.\n$mapsList")
          writeMaps(mapsList = mapsList, outDir = outDir, id = setId, v = setVersion.get)
        case None => failureExit("Metrics not received from database.")
      }
    }
  }

  class LegacyReporter(config: Config) extends Requester with LegacyExtractor with Output with Log{
    var port = 9100
    var retries = 4
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
      val metrics = Await.result(result, 60 seconds)
      val metrics_list = legacyExtract(metrics)
      setVersion match {
        case Some(v) => legacyWrite(metrics_list, config.outDir, setId, v)
        case None => failureExit("Metrics version not specified.")
      }
    }
  }
}
