import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Consumer.Control
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.Future

/**
  * Created by Conan on 5/2/17.
  */
object ReadFromConsumerAndWrite extends App{
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)
  val initialActor = classOf[ReadFromConsumerAndWrite].getName
  akka.Main.main(Array(initialActor))
}

class ReadFromConsumerAndWrite extends Actor with ActorLogging {

  import ReadFromConsumerAndWrite._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }
  override def receive: Receive = {
    case Start =>

      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("192.168.99.100:9092")
      log.info("Initializing writer")
      val kafkaSink = Producer.plainSink(producerSettings)

      val (control, future) = ReaderResource.create("ReadFromConsumerAndWrite")(context.system)
        .mapAsync(2)(processMessage)
        .map(new ProducerRecord[Array[Byte], String](ConsumerReader.Topic, _))
        .toMat(kafkaSink)(Keep.both)
        .run()

      context.become(running(control))

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }
      //   context.become(running(control))
      log.info("started")

  }
  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")

      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }

  }
  private def processMessage(msg: Message): Future[String] = {
    log.info(s"Consumed message: ${msg.record.value()}")
    Future.successful(msg.record.value())
  }
}
