package org.broadinstitute.mdreport
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpResponse}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import scala.concurrent.Future

/**
  * Created by Amr on 10/21/2016.
  * An object containing the actor system and post request logic for making http requests.
  */
object Request {
  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  private implicit val ec = system.dispatcher
  private val logger = Logger("Request")

  /**
    * A function for making JSON post requests to a database server.
    * @param path The path to the database.
    * @param json The JSON to be sent with the post request.
    * @return An HttpResponse future.
    */
  def doRequest(path: String, json: String): Future[HttpResponse] =
  {
    logger.info("Doing Request...")
    Http().singleRequest(
      Post(path, HttpEntity(contentType = `application/json`, string = json))
    )
  }
}
