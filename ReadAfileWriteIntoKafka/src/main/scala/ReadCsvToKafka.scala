import java.io._

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorSystem, Cancellable}
import akka.event.Logging
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Keep, Source}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.concurrent.duration.{Duration, _}
import scala.util.Random

/**
  * Created by Conan on 4/30/17.
  */

object ReadCsvToKafka extends App{
  case object Run
  case object Stop
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val config = ConfigFactory.load()
  val logger = Logging(system, getClass)

  val initialActor = classOf[ReadCsvToKafka].getName
  akka.Main.main(Array(initialActor))
}

class ReadCsvToKafka extends Actor with ActorLogging {

  import ReadCsvToKafka._

  override def preStart(): Unit = {
    super.preStart()
    self ! Run
  }
  val filePath = "/Users/Conan/Desktop/DexiangXu-Scala/ReadAfileWriteIntoKafka/1000-genomes.csv";

  override def receive: Receive = {
    case Run =>
      /*
      val readSource = Source.unfoldResource[String, BufferedReader](
      () => new BufferedReader(new FileReader(filePath)),
      reader => Option(reader.readLine()),
      reader => reader.close())
      */
      //do it in async way, just send the message like a mailbox and level the message there
      val readSource = Source.unfoldResourceAsync[String, BufferedReader](
        () => Promise.successful(new BufferedReader(new FileReader(filePath))).future,
        reader => Promise.successful(Option(reader.readLine())).future,
        reader => {
          reader.close()
          self ! Stop
          Promise.successful(Done).future
        }

      )
      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("192.168.99.100:9092")
      log.info("Initializing writer")
      val kafkaSink = Producer.plainSink(producerSettings)

      val (control, future) = readSource
        .map(new ProducerRecord[Array[Byte], String](CsvReader.Topic, _))
        .toMat(kafkaSink)(Keep.both)
        .run()
      future.onFailure {
        case ex =>
          log.error("Stream failed due to error, restarting", ex)
          throw ex
      }
   //   context.become(running(control))
      log.info(s"Writer now running ${CsvReader.Topic}")
  }

  def running(control: Cancellable): Receive = {
    case Stop =>
      log.info("Stopping Kafka producer stream and actor")
      control.cancel()
      context.stop(self)
  }
}


