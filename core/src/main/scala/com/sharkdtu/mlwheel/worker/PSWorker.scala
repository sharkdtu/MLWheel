package com.sharkdtu.mlwheel.worker

import akka.actor.Actor

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

private class PSWorkerActor
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = {

  }

}

class PSWorker extends Logging {

}
