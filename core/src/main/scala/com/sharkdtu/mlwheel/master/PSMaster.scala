package com.sharkdtu.mlwheel.master

import scala.collection.mutable

import akka.actor.{Actor, ActorRef, Props, Terminated}

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.message.RegisterMessages._
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}


private class PSMasterActor(manager: PSVariableMetaManager)
  extends Actor with ActorLogReceive with Logging {

  /**
   * Collection of clients available
   */
  val clients = mutable.HashSet.empty[ActorRef]

  /**
   * Collection of workers available
   */
  val workers = mutable.HashSet.empty[ActorRef]

  /**
   * The PSMasterReaderActor ref for processing ReadingMessages
   */
  private val reader = context.actorOf(Props[PSMasterReaderActor])

  /**
   * The PSMasterReaderActor ref for processing WritingMessages
   */
  private val writer = context.actorOf(Props[PSMasterWriterActor])

  override def receiveWithLogging: Receive = {
    case RegisterClient(client) =>
      logInfo(s"Registering client: ${sender.path.toString}")
      clients += client
      context.watch(client)
      sender ! true

    case RegisterWorker(worker) =>
      logInfo(s"Registering worker: ${sender.path.toString}")
      workers += worker
      context.watch(worker)
      sender ! true

    case Terminated(actor) =>
      actor match {
        case client: ActorRef if clients.contains(client) =>
          logInfo(s"Removing client: ${client.path.toString}")
          clients -= client

        case worker: ActorRef if workers.contains(worker) =>
          logInfo(s"Removing worker: ${worker.path.toString}")
          workers -= worker

        case actor: ActorRef =>
          logWarning(s"Received terminated notification from" +
            s"unknown actor ${actor.path.toString}")
      }
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