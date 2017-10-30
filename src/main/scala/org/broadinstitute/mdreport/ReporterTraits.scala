package org.broadinstitute.mdreport
import java.io.PrintWriter
import akka.http.scaladsl.model.HttpResponse
import com.typesafe.scalalogging.Logger
import org.broadinstitute.MD.rest.{MetricsQuery, SampleMetrics}
import org.broadinstitute.MD.rest.SampleMetricsRequest
import org.broadinstitute.MD.types.{BaseJson, SampleRef}
import org.broadinstitute.MD.types.metrics.MetricsType
import org.broadinstitute.mdreport.Reporters.logger
import scala.concurrent.duration._
import scala.annotation.tailrec
import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future, TimeoutException}

/**
  * Created by amr on 10/26/2016.
  * A collection of traits that can be mixed to create reporter presets.
  */
object ReporterTraits {
  val logger = Logger("ReporterTraits")

  trait Metrics {
    val setId: String
    val setVersion: Option[Long]
    val metrics: List[MetricsType.MetricsType]

    /**
      * A function for creating MetricsQuery objects.
      * @param sr a list containing SampleMetricsRequests.
      * @return
      */
    def makeMetricsQuery(sr: List[SampleMetricsRequest]): MetricsQuery = {
      MetricsQuery(
        id = setId,
        version = setVersion,
        sampleRequests = sr
      )
    }
  }

  trait Log extends Metrics with Output with Samples with Requester{
    /**
      * A function for logging reporter initialization parameter values.
      * @param l The logger object
      * @param t The reporter type as a string.
      */
    def logInit(l: Logger, t: String): Unit = {
      l.debug(List
      (s"$t Reporter Configuration",
        s"setId=$setId",
        s"setVersion=$setVersion",
        s"metrics=$metrics",
        s"samples=$sampleList")
        .mkString("\n\t")
      )
    }
  }
  trait Samples {
    val sampleList: List[String]

    /**
      * A function for generating a listbuffer of SampleRefs
      * @param srefs A mutable listbuffer for holding the accumulated sample refs.
      * @param setId the setId(aka metricsID) of the samples.
      * @return A list of SampleRefs
      */
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

    /**
      * A function for creating a listbuffer of SampleRequests
      * @param sr an iterator containign samplerefs
      * @param metrics a list indicating the MetricsTypes to request.
      * @param sreqs an initially-empty listbuffer to hold accumulated samplemetricsrequests.
      * @return a list of sample metrics requests.
      */
    def makeSampleRequests(sr: Iterator[SampleRef], metrics: List[MetricsType.MetricsType],
                           sreqs: ListBuffer[SampleMetricsRequest]): List[SampleMetricsRequest] = {
      @tailrec
      def reqAccumulator(sreqs: ListBuffer[SampleMetricsRequest]): ListBuffer[SampleMetricsRequest] = {
        if (sr.hasNext) {
          sreqs += SampleMetricsRequest(sampleRef = sr.next(), metrics = metrics)
          reqAccumulator(sreqs)
        } else {
          sreqs
        }
      }
      reqAccumulator(sreqs).toList
    }
  }

  trait Requester {
    val path: String
    var port: Int // This is var because port can be reassigned if using test DB.
    var retries: Int
    def doQuery(mq: MetricsQuery): Option[HttpResponse] = {
      val json = MetricsQuery.writeJson(mq)
      val query = Request.doRequest(path = path, json = json)
      try {
        Some(Await.result(query, 10.seconds))
      } catch {
        case e: TimeoutException => logger.error(e.getMessage)
          logger.info("Trying request again.")
          retries match {
            case 0 =>
              logger.error("Maximum retries reached.")
              None
            case _ => retries = retries - 1
              doQuery(mq)
          }
        case _: Throwable => None
      }
    }
    def doFind(id: String, version: Option[Long]): Future[HttpResponse] = {
      version match {
        case Some(v) => val json = s"""{\"id\": \"$id\", \"version\": $v}"""
          Request.doRequest(path, json)
        case None => val json = s"""{\"id\": \"$id\"}"""
          Request.doRequest(path, json)
      }
    }
  }

  trait LegacyExtractor {
    def legacyExtract(jsonList: List[BaseJson]): List[String] = {
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
    def makeMap(metricsOrder: List[String], r:List[SampleMetrics]): List[ListMap[String, Any]] = {
      def filterAndOrderMetrics(item:List[(String,Any)])
        = metricsOrder.map( (key) => item.find(_._1 == key).map(_._2).map((key,_)).getOrElse((key, None))) // return (key,None) if the sample missing the metric

      r.sortBy(_.sampleRef.sampleID).map( (item) =>
        ("sampleName", item.sampleRef.sampleID) +:                      // add ("sampleName", sampleName) to sampleMetric
          item.metrics.flatten(
            _.metric.makeValList(0, "", (k,i,v) => (k,v)))      // make a list of sampleMetric -> list of (k,v) metrics
      )
        .map( (item) => filterAndOrderMetrics(item) )                         // order and filter metric row to reporting formatting
        .map( (m) => ListMap(m.toSeq: _*) )
    }
  }

  trait Output {
    val delimiter: String
    def legacyWrite(metrics: List[String], outDir: String, id: String, v: Long): Unit = {
      val pw = new PrintWriter(s"$outDir/$id.$v.MdReport.csv")
      for (m <- metrics) pw.write(m + "\n")
      pw.close()
    }
    def writeMaps
    (mapsList: List[ListMap[String, Any]], outDir: String, id: String, v: Long): Unit = {
      val rawHeaders = mapsList.head.keysIterator
      def getHeaders(h: scala.collection.mutable.ListBuffer[String]): List[String] = {
        @tailrec
        def headerAccumulator(h: scala.collection.mutable.ListBuffer[String]): List[String] = {
          if (rawHeaders.hasNext) {
            val rawHeader = rawHeaders.next
            h += rawHeader.substring(rawHeader.lastIndexOf(".") + 1).trim()
            headerAccumulator(h)
          } else {
            val headers = h.toList
            logger.debug(s"Headers retrieved: $headers")
            headers
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