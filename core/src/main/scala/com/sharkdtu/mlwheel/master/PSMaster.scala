package com.sharkdtu.mlwheel.master

import akka.actor.{Actor, Props, Terminated}

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.message.RegisterMessages._
import com.sharkdtu.mlwheel.message.RpcRequest
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}


private class PSMasterActor(manager: PSVariableManager)
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = {
    case RegisterClientRequest(client) =>
      val resp = manager.registerClient(client)
      resp match {
        case RegisteredClient => context.watch(client)
      }
      sender ! resp

    case RegisterWorkerRequest(worker) =>
      val resp = manager.registerWorker(worker)
      resp match {
        case RegisteredWorker(_) => context.watch(worker)
      }
      sender ! resp

    case Terminated(actor) =>
      manager.remove(actor)

    case otherMsg: RpcRequest =>
      manager.process(otherMsg, sender)
  }
}

private[mlwheel] class PSMaster(conf: PSConf) extends Logging {
  // Init PSContext first.
  import com.sharkdtu.mlwheel.PSContext.Role._
  PSContext.create(conf, conf.get(PS_MASTER_HOST), MASTER)

  private def actorSystem = PSContext.get.actorSystem

  private val psVariableManager = new PSVariableManager(conf)

  /**
   * The PSMasterActor for processing rpc messages
   */
  private val master = actorSystem.actorOf(Props(new PSMasterActor(psVariableManager)),
    name = PSContext.PSMasterActorNames.psMasterActorName)


}