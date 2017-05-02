import akka.actor.{Actor, ActorLogging, ActorSystem}
import akka.event.Logging
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffsetBatch}
import akka.kafka.scaladsl.Consumer.Control
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Keep, Sink}
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object LoggingConsumer extends App {
  type Message = CommittableMessage[Array[Byte], String]
  case object Start
  case object Stop
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val initialActor = classOf[LoggingConsumer].getName
  akka.Main.main(Array(initialActor))
}

class LoggingConsumer extends Actor with ActorLogging {
  import LoggingConsumer._

  override def preStart(): Unit = {
    super.preStart()
    self ! Start
  }

  override def receive: Receive = {
    case Start =>
      log.info("Initializing logging consumer")
      val (control, future) = CsvReaderResource.create("loggingConsumer")(context.system)
        .mapAsync(2)(processMessage)
        .map(_.committableOffset)
        .groupedWithin(10, 15 seconds)
        .map(group => group.foldLeft(CommittableOffsetBatch.empty) { (batch, elem) => batch.updated(elem) })
        .mapAsync(1)(_.commitScaladsl())
        .toMat(Sink.ignore)(Keep.both)
        .run()

      context.become(running(control))

      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }

      log.info("Logging consumer started")
  }

  def running(control: Control): Receive = {
    case Stop =>
      log.info("Shutting down logging consumer stream and actor")

      control.shutdown().andThen {
        case _ =>
          context.stop(self)
      }

  }

  private def processMessage(msg: Message): Future[Message] = {
    log.info(s"Consumed message: ${msg.record.value()}")
    Future.successful(msg)
  }
}



