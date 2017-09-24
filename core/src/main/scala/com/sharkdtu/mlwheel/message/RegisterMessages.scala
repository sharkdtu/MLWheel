package com.sharkdtu.mlwheel.message

import akka.actor.ActorRef

/**
 * Messages for clients and workers registering to master
 */
private[mlwheel] object RegisterMessages {

  case class RegisterClientRequest(client: ActorRef) extends Request

  case class RegisterWorkerRequest(worker: ActorRef) extends Request

  case class RegisterClientResponse(isSuccess: Boolean, errorMsg: String) extends Response

  case class RegisterWorkerResponse(isSuccess: Boolean, errorMsg: String) extends Response
}
