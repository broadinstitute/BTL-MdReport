package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ResponseEntity
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.ActorMaterializer
import org.broadinstitute.MD.rest.SampleMetrics
import org.broadinstitute.MD.types.marshallers.Marshallers._

import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.MD.types.BaseJson
import org.broadinstitute.MD.types.metrics.Metrics


/**
  * Created by Amr on 10/21/2016.
  */
object RetrieveMetrics {

  private implicit lazy val system = ActorSystem()
  private implicit lazy val materializer = ActorMaterializer()
  private implicit lazy val ec = system.dispatcher

  def legacy_extract[T](jsonList: List[T]) = {
    var metrics: List[String] = List()
    for (x <- jsonList.toIterator) {
      x match {
        case m: Metrics => metrics = m.toCsv
        case _ =>
      }
    }
    metrics
  }
  //The function below needs to be changed so tha it can accept any type of list and process it properly.
  def extract [T](jsonList: List[T]): List[String] = {
    var metrics: List[String] = List()
    for (x <- jsonList.toIterator) {
      x match {
        case m: Metrics => metrics = m.toCsv
        case _ =>
      }
    }
    metrics
  }

  def retrieve(id: String, version: Option[Long], test: Boolean): List[String] = {
    var port = 9100
    if (test) port = 9101
    val path = s"http://btllims.broadinstitute.org:$port/MD/metricsQuery"
    version match {
      case Some(v) => doRetrieve(s"""{\"id\": \"$id\", \"version\": $v}""", path)
      case None => doRetrieve(s"""{\"id\": \"$id\"}""", path)
    }
  }

  def doRetrieve(json: String, path: String):List[String] = {

    if (path contains "find") {
      val request = new Request().doRequest(path, json)
      val response = request.flatMap(response => Unmarshal(response.entity).to[List[BaseJson]])
      val metrics = Await.result(response, 5 seconds)
      legacy_extract(metrics)
    } else {
      val request = new Request().doRequest(path, json)
      val response = request.flatMap(response => Unmarshal(response.entity).to[List[SampleMetrics]])
      val metrics = Await.result(response, 5 seconds)
      extract(metrics)
    }
  }
}
