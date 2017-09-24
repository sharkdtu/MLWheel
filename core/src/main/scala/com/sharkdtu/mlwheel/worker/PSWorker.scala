package com.sharkdtu.mlwheel.worker

import akka.actor.Actor

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.util.Utils
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}

private class PSWorkerActor
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = ???

}

class PSWorker(conf: PSConf) extends Logging {
  // Init PSContext first.
  import com.sharkdtu.mlwheel.PSContext.Role._
  PSContext.create(conf, Utils.localHostname, MASTER)
}
