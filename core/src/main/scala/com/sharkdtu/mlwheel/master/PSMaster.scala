package com.sharkdtu.mlwheel.master

import akka.actor.{Actor, Props, Terminated}

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.message.RegisterMessages._
import com.sharkdtu.mlwheel.message.Request
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}


private class PSMasterActor(manager: PSVariableManager)
  extends Actor with ActorLogReceive with Logging {

  override def receiveWithLogging: Receive = {
    case RegisterClientRequest(client) =>
      val resp = manager.registerClient(client)
      if (resp.isSuccess) context.watch(client)
      sender ! resp

    case RegisterWorkerRequest(worker) =>
      manager.registerWorker(worker)
      context.watch(worker)
      sender ! true

    case Terminated(actor) =>
      manager.remove(actor)

    case otherMsg: Request =>
      manager.process(otherMsg, sender)
  }
}

private[mlwheel] class PSMaster(conf: PSConf) extends Logging {
  // Init PSContext first.
  import com.sharkdtu.mlwheel.PSContext.Role._
  PSContext.create(conf, conf.get(PS_MASTER_HOST), MASTER)

  private def actorSystem = PSContext.get.actorSystem

  private val metaManager = new PSVariableManager(conf)

  /**
   * The PSMasterActor for processing rpc messages
   */
  private val master = actorSystem.actorOf(Props(new PSMasterActor(metaManager)),
    name = PSContext.PSMasterActorNames.psMasterActorName)


}