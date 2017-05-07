import akka.actor.ActorSystem
import akka.http.javadsl.model._
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by Conan on 5/5/2017.
  */

object ESService extends App with DefaultJsonProtocol{
  case class search()
  case class searchInfo(x :String, y : String )
  val config = ConfigFactory.load()
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val HttpConnectionFlow: Flow[HttpRequest, HttpResponse, Future[Any]]= {
    Http().outgoingConnection(config.getString("ESHttp.url"), config.getInt("ESHttp.port"))
  }

  def getFromElastic(key :String): Future[Either[String, String]] = {
    Source.single(buildRequest(key)).via(HttpConnectionFlow).runWith(Sink.head)
      .flatMap {
        response => response.status match {
          case OK => Future.successful(Right(response.entity.toString()))
          case BadRequest => Future.failed(new Exception("Bad Request"))
          case NotFound => Future.successful(Right("404 Not Found"))
          case _ => {
            println(response.status)
            Future.failed(new Exception("Failed"))}
      }
    }
  }


  def buildRequest(key: String): HttpRequest = {
    println("start searching now " + key + " loading")
    val request = RequestBuilding.Get(config.getString("ESHttp.url") + "/" + key)
    println(request)
    request
  }

  val routes = {
    pathPrefix("colaberry" / "id") {
      (get & path( Segment)) { key =>
        complete {
          val result: Future[Either[String, String]] = getFromElastic(key)
          result.map[String] {
            //right has the correct value, while left is the error message
            case Right(s) => s
            case Left(errorMessage) => errorMessage
          }
        }
      }
    }
  }
  //after Http() start the service, bindAndHandle will listen to the requests
  Http().bindAndHandle(routes, config.getString("http.link"), config.getInt("http.port"))

}
