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
      setId = Some("Mouse-Nigrovic"),
      version = Some(1485289348305L),
      sampleList = Option(List("SSF1871A09_PeterNigrovic", "SSF1871C06_PeterNigrovic"))
    )
    val ssr = new SmartSeqReporter(config)
    ssr.metrics should contain allOf (MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics, MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats, MetricsType.RnaSeqQcStats)
    ssr.setId should be ("Mouse-Nigrovic")
    ssr.setVersion should be (Some(1485289348305L))
    ssr.sampleList should contain allOf("SSF1871A09_PeterNigrovic", "SSF1871C06_PeterNigrovic")
  }
  it should "produce a correct sampleRefs" in {
    val config = Config(
      setId = Some("Mouse-Nigrovic"),
      version = Some(1485289348305L),
      sampleList = Option(List("SSF1871A09_PeterNigrovic", "SSF1871C06_PeterNigrovic"))
    )
    val ssr = new SmartSeqReporter(config)
    ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toList should contain allOf
      (
        SampleRef(sampleID = "SSF1871A09_PeterNigrovic", setID = ssr.setId),
        SampleRef(sampleID = "SSF1871C06_PeterNigrovic", setID = ssr.setId)
        )
  }
  it should "produce a correct sampleReqs" in {
    val config = Config(
      setId = Some("Mouse-Nigrovic"),
      version = Some(1485289348305L),
      sampleList = Option(List("SSF1871A09_PeterNigrovic", "SSF1871C06_PeterNigrovic"))
    )
    val ssr = new SmartSeqReporter(config)
    val sref = ssr.makeSampleRefs(setId = ssr.setId,
      srefs = scala.collection.mutable.ListBuffer[SampleRef]()).toIterator
    val sreqs = ssr.makeSampleRequests(sr = sref,
      metrics = ssr.metrics,
      sreqs = scala.collection.mutable.ListBuffer[SampleMetricsRequest]())
    sreqs should contain allOf(
      SampleMetricsRequest(SampleRef("SSF1871A09_PeterNigrovic","Mouse-Nigrovic"),
      List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
        MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
        MetricsType.RnaSeqQcStats, MetricsType.DemultiplexedStats, MetricsType.PicardEstimateLibraryComplexity)),
      SampleMetricsRequest(SampleRef("SSF1871C06_PeterNigrovic", "Mouse-Nigrovic"),
        List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
          MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
          MetricsType.RnaSeqQcStats, MetricsType.DemultiplexedStats, MetricsType.PicardEstimateLibraryComplexity))
      )
  }
    it should "produce a correct MetricsQuery" in {
      val config = Config(
        setId = Some("Mouse-Nigrovic"),
        version = Some(1485289348305L),
        sampleList = Option(List("SSF1871A09_PeterNigrovic", "SSF1871C06_PeterNigrovic"))
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
        SampleMetricsRequest(SampleRef("SSF1871A09_PeterNigrovic","Mouse-Nigrovic"),
          List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
            MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
            MetricsType.RnaSeqQcStats,  MetricsType.DemultiplexedStats, MetricsType.PicardEstimateLibraryComplexity)),
        SampleMetricsRequest(SampleRef("SSF1871C06_PeterNigrovic", "Mouse-Nigrovic"),
          List(MetricsType.PicardAlignmentSummaryAnalysis, MetricsType.PicardInsertSizeMetrics,
            MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics, MetricsType.ErccStats,
            MetricsType.RnaSeqQcStats, MetricsType.DemultiplexedStats,  MetricsType.PicardEstimateLibraryComplexity))
        )
      )
    )
    }
  it should "return a a filled map" in {
    val config = Config(
      setId = Some("Mouse-Nigrovic"),
      version = Some(1485289348305L),
      test = true,
      sampleList = Option(List("SSF1871C06_PeterNigrovic", "SSF1871D06_PeterNigrovic"))
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
    val smartseq_map = new SmartSeqReporter(config).smartseqMap
    val myMap = ssr.fillMap(smartseq_map, response)
    ssr.writeMaps(myMap, "C:\\Dev\\Scala\\MdReport\\", config.setId.get, config.version.get)
    myMap should not contain None
  }
  "A CustomReporter" should "produce a custom report object" in {
    val config = Config(
      setId = Some("Mouse-Nigrovic"),
      version = Some(1485289348305L),
      test = true,
      sampleList = Option(List("SSF1871C06_PeterNigrovic", "SSF1871D06_PeterNigrovic")),
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
    val samples = Reporters.getSamples("Mouse-Nigrovic", Some(1485289348305L), server)
    samples.length should be (96)
  }
}
