package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.ActorMaterializer
import scala.concurrent.Future

/**
  * Created by Amr on 10/21/2016.
  */
class Request {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  def doRequest(path: String, json: String): Future[HttpResponse] =
    Http().singleRequest(
      Post(uri = path, entity = HttpEntity(contentType = `application/json`, string = json))
    )
}
