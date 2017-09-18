package com.sharkdtu.mlwheel.master

import akka.actor.Actor

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging}

/**
 * Process 'client -> master' writing messages
 */
private[mlwheel] class PSMasterWriterActor
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = ???

}
