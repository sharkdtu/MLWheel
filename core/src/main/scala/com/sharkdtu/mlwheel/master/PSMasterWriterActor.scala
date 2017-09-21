package com.sharkdtu.mlwheel.master

import akka.actor.Actor

import com.sharkdtu.mlwheel.message.WritingMessages.CreateVector
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

/**
 * Process 'client -> master' writing messages
 */
private[mlwheel] class PSMasterWriterActor(manager: PSVariableMetaManager)
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = {
    case CreateVector(numDimensions, numPartitions, genFunc) =>

  }

}
