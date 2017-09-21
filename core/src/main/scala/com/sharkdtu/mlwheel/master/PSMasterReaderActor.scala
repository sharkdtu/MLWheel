package com.sharkdtu.mlwheel.master

import akka.actor.Actor

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

/**
 * Process 'client -> master' reading messages
 */
private[mlwheel] class PSMasterReaderActor(manager: PSVariableMetaManager)
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = ???

}
