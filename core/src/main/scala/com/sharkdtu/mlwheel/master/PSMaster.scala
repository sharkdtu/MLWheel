package com.sharkdtu.mlwheel.master

import akka.actor.{Actor, Props, Terminated}

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.message.RegisterMessages._
import com.sharkdtu.mlwheel.message.WritingMessages.CreateVector
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}


private class PSMasterActor(manager: PSVariableMetaManager)
  extends Actor with ActorLogReceive with Logging {

  /**
   * The PSMasterReaderActor ref for processing ReadingMessages
   */
  private val reader = context.actorOf(Props(new PSMasterReaderActor(manager)))

  /**
   * The PSMasterReaderActor ref for processing WritingMessages
   */
  private val writer = context.actorOf(Props(new PSMasterWriterActor(manager)))

  override def receiveWithLogging: Receive = {

    case RegisterClientRequest(client) =>
      manager.registerClient(client)
      context.watch(client)
      sender ! true

    case RegisterWorkerRequest(worker) =>
      manager.registerWorker(worker)
      context.watch(worker)
      sender ! true

    case Terminated(actor) =>
      manager.remove(actor)

    case msg: CreateVector =>
      writer.forward(msg)
  }

}

private[mlwheel] class PSMaster(conf: PSConf) extends Logging {
  // Init PSContext.
  import com.sharkdtu.mlwheel.PSContext.Role._
  PSContext.create(conf, conf.get(PS_MASTER_HOST), MASTER)

  private def actorSystem = PSContext.get.actorSystem

  /**
   * The PSMasterActor for monitor clients and workers
   */
  private val master = actorSystem.actorOf(Props[PSMasterActor],
    name = PSContext.PSMasterActorNames.psMasterActorName)


}

object PSMaster {
  def main(args: Array[String]): Unit = {

  }
}