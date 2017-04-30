import java.io._

import akka.actor.{Actor, ActorLogging, Cancellable}
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Source}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.util.Random

/**
  * Created by Conan on 4/30/17.
  */
class ReadCsvToKafka(implicit mat: Materializer) extends Actor with ActorLogging {

  import ReadCsvToKafka._

  override def preStart(): Unit = {
    super.preStart()
    self ! Run
  }
  val filePath = "/Users/Conan/Desktop/DexiangXu-Scala/ReadAfileWriteIntoKafka";

  override def receive: Receive = {
    case Run =>
      val readSource = Source.unfoldResource[String, BufferedReader](
        () => new BufferedReader(new FileReader(filePath)),
        reader => Option(reader.readLine()),
        reader => reader.close())

      val producerSettings = ProducerSettings(context.system, new ByteArraySerializer, new StringSerializer)
        .withBootstrapServers("localhost:2181")
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
      log.info(s"Writer now running, writing random numbers to topic ${CsvReader.Topic}")
  }

  def running(control: Cancellable): Receive = {
    case Stop =>
      log.info("Stopping Kafka producer stream and actor")
      control.cancel()
      context.stop(self)
  }
}

object ReadCsvToKafka extends App{
  case object Run
  case object Stop
}
