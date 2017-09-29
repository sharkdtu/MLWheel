package com.sharkdtu.mlwheel.message

import akka.actor.ActorRef

/**
 * Messages for clients and workers registering to master
 */
private[mlwheel] object RegisterMessages {

  // =============================== //
  //     Client Register Messages    //
  // =============================== //
  case class RegisterClientRequest(client: ActorRef) extends RpcRequest

  sealed class RegisterClientResponse extends RpcResponse

  case object RegisteredClient extends RegisterClientResponse

  case class RegisterClientFailed(errorMsg: String) extends RegisterClientResponse


  // =============================== //
  //     Worker Register Messages    //
  // =============================== //
  case class RegisterWorkerRequest(worker: ActorRef) extends RpcRequest

  sealed class RegisterWorkerResponse extends RpcResponse

  case class RegisteredWorker(id: Int) extends RegisterWorkerResponse

  case class RegisterWorkerFailed(errorMsg: String) extends RegisterWorkerResponse
}
