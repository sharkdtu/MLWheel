package com.sharkdtu.mlwheel.master

import akka.actor.Actor

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

/**
 * Process 'client -> master' reading messages
 */
class PSMasterReaderActor extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = ???

}
