package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import org.broadinstitute.MD.types.marshallers.Marshallers._

import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.MD.types.BaseJson
import org.broadinstitute.MD.types.metrics.Metrics


/**
  * Created by Amr on 10/21/2016.
  */
class RetrieveMetrics (id: String, version: Option[Long], test: Boolean) {
  var port = 9100
  if (test) port = 9101
  val path = s"http://btllims.broadinstitute.org:$port/MD/metricsQuery"
  implicit lazy val system = ActorSystem()
  implicit lazy val materializer = ActorMaterializer()
  implicit lazy val ec = system.dispatcher

  def extract [T](jsonList: List[T]): List[String] = {
    def legacy_extract() = {
      var metrics: List[String] = List()
      for (x <- jsonList.toIterator) {
        x match {
          case m: Metrics => metrics = m.toCsv
          case _ =>
        }
      }
      metrics
    }
    legacy_extract()
//    jsonList match {
//      case b: List[BaseJson] => legacy_extract()
//      case s: List[SampleMetrics] => legacy_extract()
//    }

  }
  
  def retrieve(): List[String] = {
    version match {
      case Some(v) => doRetrieve(s"""{\"id\": \"$id\", \"version\": $v}""")
      case None => doRetrieve(s"""{\"id\": \"$id\"}""")
    }
  }

  def doRetrieve(json: String):List[String] = {
    val request = new Request().doRequest(path, json)
    //val result = Await.result(request, 5 seconds)

    val response = request.flatMap(response => Unmarshal(response.entity).to[List[BaseJson]])
    //val response = request.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
    val metrics = Await.result(response, 5 seconds)
    extract(metrics)
  }
}
