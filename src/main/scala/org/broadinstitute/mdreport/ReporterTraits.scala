package org.broadinstitute.mdreport
import org.broadinstitute.MD.rest.MetricsQuery
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.SampleRef
import org.broadinstitute.MD.types.metrics.MetricsType
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

/**
  * Created by amr on 10/26/2016.
  */
object ReporterTraits {

  trait Metrics {
    val setId: String
    val setVersion: Option[Long]
    val metrics: List[MetricsType.MetricsType]

    def makeMetricsQuery(sr: ListBuffer[SampleMetricsRequest]): MetricsQuery = {
      MetricsQuery(
        id = setId,
        version = setVersion,
        sampleRequests = sr.toList
      )
    }
  }

  trait Samples {
    val sampleList: List[String]

    def makeSampleRefs(srefs: ListBuffer[SampleRef], setId: String): ListBuffer[SampleRef] = {
      val iter = sampleList.toIterator
      @tailrec
      def refAccumulator(srefs: ListBuffer[SampleRef]): ListBuffer[SampleRef] = {
        if (iter.hasNext) {
          srefs += SampleRef(sampleID = iter.next(), setID = setId)
          refAccumulator(srefs)
        } else {
          srefs
        }
      }
      refAccumulator(srefs)
    }

    def makeSampleRequests(sr: Iterator[SampleRef], metrics: List[MetricsType.MetricsType],
                           sreqs: ListBuffer[SampleMetricsRequest]): ListBuffer[SampleMetricsRequest] = {
      @tailrec
      def reqAccumulator(sreqs: ListBuffer[SampleMetricsRequest]): ListBuffer[SampleMetricsRequest] = {
        if (sr.hasNext) {
          sreqs += SampleMetricsRequest(sampleRef = sr.next(), metrics = metrics)
          reqAccumulator(sreqs)
        } else {
          sreqs
        }
      }
      reqAccumulator(sreqs)
    }
  }

  trait Query {
    val path: String
    def doQuery(mq: MetricsQuery) = {
      val request = new Request()
      val json = MetricsQuery.writeJson(mq)
      request.doRequest(path = path, json = json)
    }
  }

  trait Output {
//    val bookName: String
//    val workbook =  Workbook().saveAsXlsx(bookName)
//    def makeWorkbook = {
//    }
  }
}