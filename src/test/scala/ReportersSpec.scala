import org.scalatest.{FlatSpec, Matchers}
import org.broadinstitute.MD.rest.MetricsQuery
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.metrics.MetricsType
import org.broadinstitute.mdreport.Config
import org.broadinstitute.mdreport.Reporters._
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
/**
  * Created by amr on 11/2/2016.
  */

class ReportersSpec extends FlatSpec with Matchers{

  "A SmartSeqReporter" should "be created completely from a config object" in {
    val config = Config(
      setId = "SSF-1859",
      version = Some(7),
      sampleList = Iterator("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")
    )
    val ssr = new SmartSeqReporter(config)
    ssr.metrics should contain allOf (MetricsType.PicardAlignmentSummaryAnalysis,
      MetricsType.PicardInsertSizeMetrics, MetricsType.PicardMeanQualByCycle, MetricsType.PicardReadGcMetrics,
      MetricsType.ErccStats, MetricsType.RnaSeqQcStats)
    ssr.setId should be ("SSF-1859")
    ssr.setVersion should be (Some(7))
    ssr.sampleList.toList should contain allOf("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")
  }
  it should "produce a correct sampleRefs" in {
    val config = Config(
      setId = "SSF-1859",
      version = Some(7),
      sampleList = Iterator("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")
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
      setId = "SSF-1859",
      version = Some(7),
      sampleList = Iterator("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")
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
        setId = "SSF-1859",
        version = Some(7),
        sampleList = Iterator("SSF1859B12_A375_AkiYoda", "SSF1859A11_A375_AkiYoda")
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
}
