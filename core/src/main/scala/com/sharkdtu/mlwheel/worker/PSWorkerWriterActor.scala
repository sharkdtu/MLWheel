package com.sharkdtu.mlwheel.worker

import akka.actor.Actor

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

/**
 * Process 'master -> worker' writing messages
 */
class PSWorkerWriterActor extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = ???

}
