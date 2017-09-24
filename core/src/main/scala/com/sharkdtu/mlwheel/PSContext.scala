package com.sharkdtu.mlwheel

import akka.actor.ActorSystem

import com.sharkdtu.mlwheel.conf._
import com.sharkdtu.mlwheel.conf.PSConf
import com.sharkdtu.mlwheel.serializer.{JavaSerializer, Serializer}
import com.sharkdtu.mlwheel.util.{AkkaUtils, ClosureUtils, Utils}

/**
 * A context which wraps some necessary components for client, master, worker.
 *
 * @note It is not intended for external use.
 */
private[mlwheel] class PSContext private(
    val conf: PSConf,
    val serializer: Serializer,
    val closureSerializer: Serializer,
    val actorSystem: ActorSystem
  ) extends Logging {

  def stop(): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    actorSystem.terminate().foreach { _ =>
      logInfo("Actor system was shut down.")
    }
  }

}

private[mlwheel] object PSContext extends Logging {

  @volatile private var _context: PSContext = _

  val psClientActorSystemName = "ps-client"
  val psMasterActorSystemName = "ps-master"
  val psWorkerActorSystemName = "ps-worker"

  val PS_VARIABLE_FAKE_ID = "fake-id"

  object Role extends Enumeration {
    type Role = Value
    val CLIENT, MASTER, WORKER = Value
  }
  import Role._

  object PSMasterActorNames {
    val psMasterActorName = "receptionist"
  }

  object PSWorkerActorNames {
    val psWorkerActorName = "receptionist"
  }

  /**
   * Returns the only [[PSContext]].
   */
  def get: PSContext = {
    require(_context != null, "PSContext has not been created before.")
    _context
  }

  /**
   * The factory method of creating [[PSContext]]
   *
   * @note [[PSContext]] can not be created in multi-threads, and it can be created only once.
   */
  def create(conf: PSConf, host: String, role: Role): PSContext = {
    if (_context != null) {
      logWarning("A PSContext object has been created before.")
      return _context
    }

    // Init ActorSystem
    val (actorSystem, boundPort) = {
      val (actorSystemName, port) = role match {
        case CLIENT => (psClientActorSystemName, conf.get(PS_CLIENT_PORT))
        case MASTER => (psMasterActorSystemName, conf.get(PS_MASTER_PORT))
        case WORKER => (psWorkerActorSystemName, conf.get(PS_WORKER_PORT))
      }
      AkkaUtils.createActorSystem(actorSystemName, host, port, conf)
    }

    // Figure out which port Akka actually bound to in case the original port is 0 or occupied.
    role match {
      case CLIENT => conf.set(PS_CLIENT_PORT, boundPort)
      case MASTER => conf.set(PS_MASTER_PORT, boundPort)
      case WORKER => conf.set(PS_WORKER_PORT, boundPort)
    }

    // Create an instance of the class with the given name, possibly initializing it with our conf
    def instantiateClass[T](className: String): T = {
      val cls = Utils.classForName(className)
      // Look for a constructor taking a PSConf and a boolean isDriver, then one taking just
      // SparkConf, then one taking no arguments
      try {
        cls.getConstructor(classOf[PSConf])
          .newInstance(conf)
          .asInstanceOf[T]
      } catch {
        case _: NoSuchMethodException =>
          cls.getConstructor().newInstance().asInstanceOf[T]
      }
    }

    val serializer = instantiateClass[Serializer](conf.get(PS_SERIALIZER))

    // Only support JavaSerializer for closure
    val closureSerializer = new JavaSerializer(conf)

    _context = new PSContext(conf, serializer, closureSerializer, actorSystem)
    logInfo("Successfully create PSContext instance.")
    _context
  }

}