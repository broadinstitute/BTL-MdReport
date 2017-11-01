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
import org.broadinstitute.mdreport.Reporters
import scala.language.postfixOps
/**
  * Created by amr on 11/2/2016.
  */

class ReportersSpec extends FlatSpec with Matchers{
  private implicit lazy val system = ActorSystem()
  private implicit lazy val materializer = ActorMaterializer()
  private implicit lazy val ec = system.dispatcher
  "A SmartSeqReporter" should "be created completely from a config object" in {
    val config = Config(
      setId = Some("SSF-11680_SmartSeqAnalysisV1.1_1"),
      version = Some(1507901946874L),
      sampleList = Option(List("000008534669_B04", "000008534669_D05"))
    )
    val ssr = new SmartSeqReporter(config)
    ssr.metrics should contain allOf (MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics, MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats, MetricsType.RnaSeqQcStats, MetricsType.SampleSheet)
    ssr.setId should be ("SSF-11680_SmartSeqAnalysisV1.1_1")
    ssr.setVersion should be (Some(1507901946874L))
    ssr.sampleList should contain allOf("000008534669_B04", "000008534669_D05")
  }
  it should "produce a correct sampleRefs" in {
    val config = Config(
      setId = Some("SSF-11680_SmartSeqAnalysisV1.1_1"),
      version = Some(1507901946874L),
      sampleList = Option(List("000008534669_B04", "000008534669_D05"))
    )
    val ssr = new SmartSeqReporter(config)
    ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toList should contain allOf
      (
        SampleRef(sampleID = "000008534669_B04", setID = ssr.setId),
        SampleRef(sampleID = "000008534669_D05", setID = ssr.setId)
        )
  }
  it should "produce a correct sampleReqs" in {
    val config = Config(
      setId = Some("SSF-11680_SmartSeqAnalysisV1.1_1"),
      version = Some(1507901946874L),
      sampleList = Option(List("000008534669_B04", "000008534669_D05"))
    )
    val ssr = new SmartSeqReporter(config)
    val sref = ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
    val sreqs = ssr.makeSampleRequests(sr = sref,
      metrics = ssr.metrics,
      sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
    sreqs should contain allOf(
      SampleMetricsRequest(SampleRef("000008534669_B04","SSF-11680_SmartSeqAnalysisV1.1_1"),
      List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
        MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
        MetricsType.RnaSeqQcStats, MetricsType.DemultiplexedMultipleStats, MetricsType.PicardEstimateLibraryComplexity,
        MetricsType.SampleSheet)),
      SampleMetricsRequest(SampleRef("000008534669_D05", "SSF-11680_SmartSeqAnalysisV1.1_1"),
        List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
          MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
          MetricsType.RnaSeqQcStats, MetricsType.DemultiplexedMultipleStats, MetricsType.PicardEstimateLibraryComplexity,
          MetricsType.SampleSheet))
      )
  }
    it should "produce a correct MetricsQuery" in {
      val config = Config(
        setId = Some("SSF-11680_SmartSeqAnalysisV1.1_1"),
        version = Some(1507901946874L),
        sampleList = Option(List("000008534669_B04", "000008534669_D05"))
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
        SampleMetricsRequest(SampleRef("000008534669_B04","SSF-11680_SmartSeqAnalysisV1.1_1"),
          List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
            MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
            MetricsType.RnaSeqQcStats,  MetricsType.DemultiplexedMultipleStats, MetricsType.PicardEstimateLibraryComplexity,
            MetricsType.SampleSheet)),
        SampleMetricsRequest(SampleRef("000008534669_D05", "SSF-11680_SmartSeqAnalysisV1.1_1"),
          List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
            MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
            MetricsType.RnaSeqQcStats, MetricsType.DemultiplexedMultipleStats,  MetricsType.PicardEstimateLibraryComplexity,
            MetricsType.SampleSheet))
        )
      )
    )
    }
  it should "return a a filled map" in {
    val samples = List("000008534669_F07","000008534669_B04", "000008534669_D05", "000008534669_F01",
      "000008534669_A09", "000008534669_F04").sorted
    val config = Config(
      setId = Some("SSF-11680_SmartSeqAnalysisV1.1_1"),
      version = Some(1507901946874L),
      port = 9101,
      sampleList = Option(samples)
    )
    val ssr = new SmartSeqReporter(config)
    val sref = ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
    val sreqs = ssr.makeSampleRequests(sr = sref,
      metrics = ssr.metrics,
      sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
    val mq = ssr.makeMetricsQuery(sreqs)
    val query = ssr.doQuery(mq)
    query match {
      case Some(r) =>
        val result = Unmarshal(r.entity).to[List[SampleMetrics]]
        val response = Await.result(result, 60 seconds)
        val smartseq_order = new SmartSeqReporter(config).smartseqOrder
        val myMap = ssr.makeMap(smartseq_order, response)
        ssr.writeMaps(myMap, "C:\\Dev\\Scala\\MdReport\\", config.setId.get, config.version.get)
        myMap should not contain None
      case None => None
    }

  }
  "A CustomReporter" should "produce a custom report object" in {
    val config = Config(
      setId = Some("SSF-11680_SmartSeqAnalysisV1.1_1"),
      version = Some(1507901946874L),
      port = 9101,
      sampleList = Option(List("000008534669_D05", "SSF1871D06_PeterNigrovic")),
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
    val samples = Reporters.getSamples("SSF-11680_SmartSeqAnalysisV1.1_1", Some(1507901946874L), server)
    samples.length should be (96)
  }
}
