package com.sharkdtu.mlwheel.worker

import akka.actor.Actor

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

/**
 * Process 'client -> worker' reading messages
 */
class PSWorkerReaderActor extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = ???

}
