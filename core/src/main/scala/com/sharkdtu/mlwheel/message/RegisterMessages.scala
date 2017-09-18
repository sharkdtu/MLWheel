package com.sharkdtu.mlwheel.message

import akka.actor.ActorRef

/**
 * Messages for clients and workers registering to master
 */
private[mlwheel] object RegisterMessages {

  case class RegisterClient(client: ActorRef)

  case class RegisterWorker(worker: ActorRef)

}
