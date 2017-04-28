package com.example

import akka.actor.{Actor, ActorLogging, Props}


class PingActor extends Actor with ActorLogging {
  //import everything that's included in PingActor object
  import PingActor._
  
  var counter = 0
  val pongActor = context.actorOf(PongActor.props, "pongActor")

  def receive = {
  	case Initialize => 
	    log.info("In PingActor - starting ping-pong")
  	  pongActor ! PingMessage("ping")	
  	case PongActor.PongMessage(text) =>
  	  log.info("In PingActor - received message: {}", text)
  	  counter += 1
  	  if (counter == 3) context.system.shutdown()

      //sender will response to who ever send the message
      else sender() ! PingMessage("ping")
  }	
}

object PingActor {
  //Props is a configuration object using in creating an Actor; it is immutable, so it is thread-safe and fully shareable.
  val props = Props[PingActor]  //used to set the PingActor
  case object Initialize
  case class PingMessage(text: String)
}