package com.sharkdtu.mlwheel.master

import java.util.concurrent.TimeUnit

import scala.collection.mutable

import akka.actor.ActorRef

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.util.{AkkaUtils, ThreadUtils}

/**
 * A PSVariable metadata manager.
 */
private[mlwheel] class PSVariableMetaManager(conf: PSConf)
  extends Logging {

  // Collection of client actor refs available
  private val clients = mutable.HashSet.empty[ActorRef]

  // Collection of worker actor refs available
  private val workers = mutable.HashSet.empty[ActorRef]

  // id -> PSVariableMeta
  type PSVariableMetaMap = mutable.HashMap[Int, PSVariableMeta]

  // client id(hostport) -> all ps variables
  private val psVariables = mutable.HashMap.empty[String, PSVariableMetaMap]

  private val cleanThread = ThreadUtils.newDaemonSingleThreadScheduledExecutor("client-cleaner")

  cleanThread.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      //TODO clean client meta
    }
  }, 0, conf.get(PS_CLIENT_CLEANER_INTERVAL), TimeUnit.MILLISECONDS)

  /**
   * Register a client.
   *
   * @param client The client actor ref
   */
  def registerClient(client: ActorRef): Unit = {
    val clientId = AkkaUtils.getHostPort(client)
    logInfo(s"Registering client: $clientId")
    clients += client
    psVariables.getOrElseUpdate(clientId, new PSVariableMetaMap())
  }

  /**
   * Register a worker.
   *
   * @param worker The worker actor ref
   */
  def registerWorker(worker: ActorRef): Unit = {
    val workerId = AkkaUtils.getHostPort(worker)
    logInfo(s"Registering worker: $workerId")
    workers += worker
  }

  /**
   * Remove a client or worker.
   *
   * @param actor The actor to be removed
   */
  def remove(actor: ActorRef): Unit = {
    if (clients.contains(actor)) {
      removeClient(actor)
    } else if (workers.contains(actor)) {
      removeWorker(actor)
    } else {
      logWarning(s"Ignore terminated notification" +
        s"from unknown actor: ${actor.path.toString}")
    }
  }

  /**
   * Remove a client.
   *
   * @param client The client to be removed
   */
  private def removeClient(client: ActorRef): Unit = {
    val clientId = AkkaUtils.getHostPort(client)
    logInfo(s"Removing client: $clientId")
    clients -= client
  }

  /**
   * Remove a worker.
   *
   * @param worker The worker to be removed
   */
  private def removeWorker(worker: ActorRef): Unit = {
    val workerId = AkkaUtils.getHostPort(worker)
    logInfo(s"Removing worker: $workerId")
    workers -= worker
  }

  def addPSVector(clientId: String): Unit = {

  }

}

private case class PSVariableMeta(variableId: Int, partitions: Array[PartitionMeta]) {
  def numPartitions: Int = partitions.length
}

// TODO: multi copies for each partition
private case class PartitionMeta(partitionId: Int, worker: ActorRef)
