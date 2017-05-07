import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Keep, Sink}
import com.typesafe.config.ConfigFactory
import akka.util.ByteString
import akka.http.javadsl.model._
import akka.http.javadsl.model.headers._
import java.util.Optional

import akka.http.scaladsl.Http

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpMethods._

/**
  * Created by Conan on 5/3/2017.
  */

object ElasticSearchProducer extends App {
  implicit  val system = ActorSystem()
  implicit  val executor = system.dispatcher
  implicit  val materializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop

  val initialActor = classOf[ElasticSearchProducer].getName
  akka.Main.main(Array(initialActor))
}

class ElasticSearchProducer extends Actor with ActorLogging{
  import ElasticSearchProducer._

//  read header only
  val headerString = scala.io.Source.fromFile(config.getString("inputFile.filename")).getLines().next()
  val headerList = CSVParser.parse(headerString,'\\', ',' , '"')

  override def preStart(): Unit ={
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing ElasticSearch producer ...")

      val done = ESConsumerSource.create("logging Consumer")(context.system)
        .mapAsync(2)(processMessage)
        .via(connectionFlow).runWith(Sink.ignore)
  }

  private def writeIntoES (jsonString: String, id:String): HttpRequest = {
    val request = HttpRequest(
      POST,
      uri = config.getString("elastic.uri") + "/" + id,
      //Creat json object
      entity = HttpEntity(ContentTypes.`application/json`,ByteString(jsonString))
    )
    request
  }

  private def processMessage(msg:Message): Future[HttpRequest] = {
    val elementsList = CSVParser.parse(msg.record.value(), '\\', ',', '"').get
    val id = elementsList(0).toString.replace("\"","")
    val combinedWithHeader = headerList.get.zip(elementsList.toList)
    val formatCombinedList = combinedWithHeader.map(item => "\"" + item._1.toString().replace("\"", "") + "\":\"" + JsonValueFormatter(item._2.toString()) + "\"")
    val jsonString = "{ " + formatCombinedList.mkString(",") + "}"
    Future.successful(writeIntoES(jsonString, id))
  }

  private def JsonValueFormatter(str: String ): String = {
    if (str.indexOf("\"").equals(-1)) {
      if (str.length() == 0) ""
      else str
    } else {
      "\"" + str.replace("\"", "")
    }
  }

  private def connectionFlow:Flow[HttpRequest, HttpResponse, Any] = {
    //Connecting to the elastic search
    Http().outgoingConnection(config.getString("http.interface"), config.getInt("http.port"))
  }
}
