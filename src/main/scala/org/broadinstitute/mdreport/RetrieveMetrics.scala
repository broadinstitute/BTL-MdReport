package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.broadinstitute.MD.types.marshallers.Marshallers._
import scala.concurrent.duration._
import scala.concurrent.Await
import org.broadinstitute.MD.types.BaseJson
import org.broadinstitute.MD.types.metrics.Metrics
/**
  * Created by Amr on 10/21/2016.
  */
class RetrieveMetrics (id: String, version: Long, test: Boolean) {
  var port = 9100
  if (test) port = 9101
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
//  val json = s"""{\"id\": \"$id\", \"version\": $version}"""
//  val path = s"http://btllims.broadinstitute.org:$port/MD/find/metrics"

  def extract (jsonList: List[BaseJson]): List[String] = {
    var metrics: List[String] = List()
    for (x <- jsonList.toIterator) {
      x match {
        case m: Metrics => metrics = m.toCsv
        case _ =>
      }
    }
  metrics
  }
  def retrieve(json: String = s"""{\"id\": \"$id\", \"version\": $version}""",
               path: String = s"http://btllims.broadinstitute.org:$port/MD/find/metrics"): List[String] = {
    val request = new Request().doRequest(path, json)
    val response = request.flatMap(response => Unmarshal(response.entity).to[List[BaseJson]])
    val metrics = Await.result(response, 10 seconds)
    extract(metrics)
  }
}
