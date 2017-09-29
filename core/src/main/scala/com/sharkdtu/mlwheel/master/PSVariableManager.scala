package com.sharkdtu.mlwheel.master

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable

import akka.actor.ActorRef

import com.sharkdtu.mlwheel.{Logging, PSContext}
import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.message.RegisterMessages._
import com.sharkdtu.mlwheel.message.RpcRequest
import com.sharkdtu.mlwheel.message.WritingMessages._
import com.sharkdtu.mlwheel.parameter.PSVariable
import com.sharkdtu.mlwheel.util.{AkkaUtils, ThreadUtils, Utils}

/**
 * Center PSVariable manager.
 */
private[mlwheel] class PSVariableManager(conf: PSConf)
  extends Logging {

  private val nextWorkerId = new AtomicInteger(0)

  // Collection of client actor refs available
  private val clients = mutable.HashSet.empty[ActorRef]

  // worker -> workerId
  private val workers = mutable.HashMap.empty[ActorRef, Int]

  // PSVariableId -> PSVariable
  type PSVariableMap = mutable.HashMap[String, PSVariable]

  // All ps variables, clientId -> PSVariableMap
  private val psVariables = mutable.HashMap.empty[String, PSVariableMap]

  private val msgProcessor = ThreadUtils.newDaemonCachedThreadPool("msg-processor",
    conf.get(PS_MASTER_MESSAGE_PROCESSOR_POOL_SIZE))

  private val clientCleaner = ThreadUtils.newDaemonSingleThreadScheduledExecutor("client-cleaner")

  clientCleaner.scheduleAtFixedRate(new Runnable {
    override def run(): Unit = {
      //TODO clean meta
    }
  }, 0, conf.get(PS_MASTER_CLIENT_CLEANER_INTERVAL), TimeUnit.MILLISECONDS)

  /**
   * Register a client.
   *
   * @param client The client actor ref
   */
  def registerClient(client: ActorRef): RegisterClientResponse = {
    logInfo(s"Registering client: $client")
    val maxClients = conf.get(PS_MASTER_CLIENT_CAPACITY)
    if (clients.size == maxClients) {
      // Exceed maximum clients, register failed!
      RegisterClientFailed(s"Already have $maxClients clients.")
    } else {
      clients += client
      RegisteredClient
    }
  }

  /**
   * Get the unique id of client.
   */
  private def getClientId(client: ActorRef): String = {
    s"${AkkaUtils.getHostPort(client)}#${AkkaUtils.getUid(client)}"
  }

  /**
   * Register a worker.
   *
   * @param worker The worker actor ref
   */
  def registerWorker(worker: ActorRef): RegisterWorkerResponse = {
    logInfo(s"Registering worker: $worker")
    val maxWorkers = conf.get(PS_MASTER_WORKER_CAPACITY)
    if (workers.contains(worker)) {
      RegisterWorkerFailed("Worker has been registered.")
    } else if(workers.size == maxWorkers) {
      RegisterWorkerFailed(s"Already have $maxWorkers workers.")
    } else {
      val workerId = nextWorkerId.getAndIncrement()
      workers.put(worker, workerId)
      RegisteredWorker(workerId)
    }
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
    logInfo(s"Removing client: $client")
    clients -= client
  }

  /**
   * Remove a worker.
   *
   * @param worker The worker to be removed
   */
  private def removeWorker(worker: ActorRef): Unit = {
    logInfo(s"Removing worker: $worker")
    // TODO deliver its data to other worker
    workers -= worker
  }

  /**
   * Process request messages from clients
   *
   * @param msg The message to be processed
   * @param sender The message sender
   */
  def process(msg: RpcRequest, sender: ActorRef): Unit = {
    msg match {
      case CreateVectorRequest(clientId, numDims, numParts, partMode, genFunc) =>
        msgProcessor.execute(new Runnable {
          override def run(): Unit = Utils.logUncaughtExceptions {
            val registered = clients.exists(getClientId(_) == clientId)
            if (!registered) {
              sender ! CreateVectorResponse(PSContext.PS_VARIABLE_FAKE_ID,
                s"Client($clientId) may terminated before.")
            } else {
              // TODO
            }
          }
        })
    }
  }

  def addPSVector(clientId: String): Unit = {

  }

}
