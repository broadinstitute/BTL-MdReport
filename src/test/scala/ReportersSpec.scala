import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}
import org.broadinstitute.MD.rest.{MetricsQuery, SampleMetrics}
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.metrics.MetricsType
import org.broadinstitute.mdreport.Config
import org.broadinstitute.mdreport.Reporters._
import org.broadinstitute.MD.rest.SampleMetricsRequest
import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.MD.types.marshallers.Marshallers._
import org.broadinstitute.mdreport.ReporterTraits.Requester
import scala.collection.mutable
import org.broadinstitute.mdreport.Reporters

/**
  * Created by amr on 11/2/2016.
  */

class ReportersSpec extends FlatSpec with Matchers{
  private implicit lazy val system = ActorSystem()
  private implicit lazy val materializer = ActorMaterializer()
  private implicit lazy val ec = system.dispatcher
  "A SmartSeqReporter" should "be created completely from a config object" in {
    val config = Config(
      setId = Some("SSF-1859"),
      version = Some(7),
      sampleList = Option(List("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda"))
    )
    val ssr = new SmartSeqReporter(config)
    ssr.metrics should contain allOf (MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics, MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats, MetricsType.RnaSeqQcStats)
    ssr.setId should be ("SSF-1859")
    ssr.setVersion should be (Some(7))
    ssr.sampleList should contain allOf("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")
  }
  it should "produce a correct sampleRefs" in {
    val config = Config(
      setId = Some("SSF-1859"),
      version = Some(7),
      sampleList = Option(List("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda"))
    )
    val ssr = new SmartSeqReporter(config)
    ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toList should contain allOf
      (
        SampleRef(sampleID = "SSF1859B12_A375_AkiYoda", setID = ssr.setId),
        SampleRef(sampleID = "SSF1859A11_A375_AkiYoda", setID = ssr.setId)
        )
  }
  it should "produce a correct sampleReqs" in {
    val config = Config(
      setId = Some("SSF-1859"),
      version = Some(7),
      sampleList = Option(List("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda"))
    )
    val ssr = new SmartSeqReporter(config)
    val sref = ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
    val sreqs = ssr.makeSampleRequests(sr = sref,
      metrics = ssr.metrics,
      sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
    sreqs should contain allOf(
      SampleMetricsRequest(SampleRef("SSF1859B12_A375_AkiYoda","SSF-1859"),
      List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
        MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
        MetricsType.RnaSeqQcStats)),
      SampleMetricsRequest(SampleRef("SSF1859A11_A375_AkiYoda", "SSF-1859"),
        List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
          MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
          MetricsType.RnaSeqQcStats))
      )
  }
    it should "produce a correct MetricsQuery" in {
      val config = Config(
        setId = Some("SSF-1859"),
        version = Some(7),
        sampleList = Option(List("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda"))
      )
      val ssr = new SmartSeqReporter(config)
      val sref = ssr.makeSampleRefs(setId = ssr.setId,
        srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
      val sreqs = ssr.makeSampleRequests(sr = sref,
        metrics = ssr.metrics,
        sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
      val mq = ssr.makeMetricsQuery(sreqs)
      mq should be (
        MetricsQuery(
          id = ssr.setId,
          version = ssr.setVersion,
          List(
        SampleMetricsRequest(SampleRef("SSF1859B12_A375_AkiYoda","SSF-1859"),
          List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
            MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
            MetricsType.RnaSeqQcStats)),
        SampleMetricsRequest(SampleRef("SSF1859A11_A375_AkiYoda", "SSF-1859"),
          List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
            MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
            MetricsType.RnaSeqQcStats))
        )
      )
    )
    }
  it should "return a a filled map" in {
    val config = Config(
      setId = Some("SSF-1859"),
      version = Some(7),
      test = true,
      sampleList = Option(List("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda"))
    )
    val ssr = new SmartSeqReporter(config)
    val sref = ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
    val sreqs = ssr.makeSampleRequests(sr = sref,
      metrics = ssr.metrics,
      sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
    val mq = ssr.makeMetricsQuery(sreqs)
    val query = ssr.doQuery(mq)
    val result = query.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
    val response = Await.result(result, 5 seconds)
    val smartseq_map: mutable.LinkedHashMap[String, Any] = mutable.LinkedHashMap(
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
    val myMap = ssr.fillMap(smartseq_map, response)
    ssr.writeMaps(myMap, "C:\\Dev\\Scala\\MdReport\\", config.setId.get, config.version.get)
    class getSamples extends Requester {
      var port = 9101
      val path = s"http://btllims.broadinstitute.org:$port/MD/find/metrics"
      def run() = {
        doFind("SSF-1859", None)
      }
    }
    myMap should not contain None
  }
  "A CustomReporter" should "produce a custom report object" in {
    val config = Config(
      setId = Some("SSF-1859"),
      version = Some(7),
      test = true,
      sampleList = Option(List("SSF1859B10_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")),
      rdfFile = Some("C:\\Dev\\Scala\\MdReport\\src\\test\\resources\\rdf.tsv"),
      outDir = "C:\\Dev\\Scala\\MdReport\\"
    )
    val cr = new CustomReporter(config)
    val delim = "\t"
    val out = scala.io.Source.fromFile(config.rdfFile.get).getLines.map(x =>
      {
        val iter = x.split(delim)
        Tuple2(MetricsType.withName(iter.head), iter.last.split(","))
      }
    )
    cr.run()
    out.length should be (3)
  }
  "A Sample Metrics Query" should "return a list of sample names" in {
    val rootPath = "http://btllims.broadinstitute.org"
    val port = 9101
    val server = s"$rootPath:$port/MD"
    val samples = Reporters.getSamples("SSF-1859", Some(7), server)
    samples should contain allOf("SSF1859B12_A375_AkiYoda", "SSF1859A12_A375_AkiYoda", "SSF1859B07_A375_AkiYoda",
      "SSF1859B09_A375_AkiYoda", "SSF1859B11_A375_AkiYoda", "SSF1859B04_A375_AkiYoda", "SSF1859B05_A375_AkiYoda",
      "SSF1859A07_A375_AkiYoda", "SSF1859A10_A375_AkiYoda", "SSF1859B08_A375_AkiYoda", "SSF1859A11_A375_AkiYoda",
      "SSF1859A05_A375_AkiYoda", "SSF1859A06_WBC_AkiYoda", "SSF1859A09_A375_AkiYoda", "SSF1859B10_A375_AkiYoda",
      "SSF1859B06_A375_AkiYoda", "SSF1859A08_A375_AkiYoda", "SSF1859A04_A375_AkiYoda", "SSF1859A02_A375_AkiYoda",
      "SSF1859B02_A375_AkiYoda", "SSF1859B01_A375_AkiYoda", "SSF1859B03_A375_AkiYoda", "SSF1859A03_A375_AkiYoda",
      "SSF1859A01_A375_AkiYoda")

  }
}
