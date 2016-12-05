package org.broadinstitute.mdreport
import java.io.PrintWriter
import akka.http.scaladsl.model.HttpResponse
import org.broadinstitute.MD.rest.{MetricsQuery, SampleMetrics}
import org.broadinstitute.MD.rest.MetricsQuery.SampleMetricsRequest
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import org.broadinstitute.MD.types.metrics.MetricsType
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

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

  trait Requester {
    val path: String
    var port: Int // This is var because port can be reassigned if using test DB.
    def doQuery(mq: MetricsQuery): Future[HttpResponse] = {
      val request = new Request()
      val json = MetricsQuery.writeJson(mq)
      request.doRequest(path = path, json = json)
    }
    def doFind(id: String, version: Option[Long]): Future[HttpResponse] = {
      version match {
        case Some(v) => val json = s"""{\"id\": \"$id\", \"version\": $v}"""
          new Request().doRequest(path, json)
        case None => val json = s"""{\"id\": \"$id\"}"""
          new Request().doRequest(path, json)
      }
    }
//    def getSamples(id: String, version: Option[Long]): Future[HttpResponse] = {
//      version match {
//        case Some(v) => val json = s"""{\"id\": \"$id\", \"version\": $v}"""
//          new Request().doRequest(path, json)
//        case None => val json = s"""{\"id\": \"$id\"}"""
//          new Request().doRequest(path, json)
//      }
//    }
  }

  trait LegacyExtractor {
    def legacyExtract(jsonList: List[BaseJson]) = {
      var metrics: List[String] = List()
      for (x <- jsonList.toIterator) {
        x match {
          case m: org.broadinstitute.MD.types.metrics.Metrics => metrics = m.toCsv
          case _ =>
        }
      }
      metrics
    }
  }

  trait MapMaker {
    /*TODO: Need to figure out a way to properly figure out use of index to solve situations where
    we need to explicitly get the right totalReads(PAIR) value. Currently just relying on the right one
    being the last one, which isn't a good idea.
     */
    def fillMap(m: mutable.LinkedHashMap[String, Any], r: List[SampleMetrics]) = {
      var maps = mutable.ListBuffer[ListMap[String, Any]]()
      for (item <- r) {
        m("sampleName") = item.sampleRef.sampleID
        for (x <- item.metrics) {
          x.metric.makeValList(0, "", (k, i, v) => {
            if (m.contains(k)) m(k) = v
          }
          )
        }
        //For some reason m mutates prior to entering into listbuffer. This converts m to immutable. Copied from:
        // http://stackoverflow.com/questions/6199186/scala-linkedhashmap-tomap-preserves-order
        def toMap[A, B](lhm: mutable.LinkedHashMap[A, B]): ListMap[A, B] = ListMap(lhm.toSeq: _*)
        val lm = toMap(m)
        maps += lm
      }
      maps.toList
    }
  }

  trait Output {
    val delimiter: String
    def legacyWrite(metrics: List[String], outDir: String, id: String, v: Long) = {
      val pw = new PrintWriter(s"$outDir/$id.$v.MdReport.csv")
      for (m <- metrics) pw.write(m + "\n")
      pw.close()
    }
    def writeMaps
    (mapsList: List[ListMap[String, Any]], outDir: String, id: String, v: Long) = {
      val rawHeaders = mapsList.head.keysIterator
      def getHeaders(h: scala.collection.mutable.ListBuffer[String]): List[String] = {
        @tailrec
        def headerAccumulator(h: scala.collection.mutable.ListBuffer[String]): List[String] = {
          if (rawHeaders.hasNext) {
            val rawHeader = rawHeaders.next
            h += rawHeader.substring(rawHeader.lastIndexOf(".") + 1).trim()
            headerAccumulator(h)
          } else {
            h.toList
          }
        }
        headerAccumulator(h)
      }
      def writeLines(pw: PrintWriter, headers: List[String]) = {
        pw.write(headers.mkString(delimiter).concat("\n"))
        for (map <- mapsList) {
          pw.write(map.values.mkString(delimiter).concat("\n"))
        }
        pw.close()
      }
      val headers = getHeaders(scala.collection.mutable.ListBuffer[String]())
      delimiter match {
        case "," =>
          val pw = new PrintWriter(s"$outDir/$id.$v.MdReport.csv")
          writeLines(pw, headers)
        case "\t" =>
          val pw = new PrintWriter(s"$outDir/$id.$v.MdReport.tsv")
          writeLines(pw, headers)
        case _ =>
          val pw = new PrintWriter(s"$outDir/$id.$v.MdReport.txt")
          writeLines(pw, headers)
      }
    }
  }
}