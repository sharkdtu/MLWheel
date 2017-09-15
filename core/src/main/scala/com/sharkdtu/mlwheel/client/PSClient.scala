package com.sharkdtu.mlwheel.client

import akka.actor.{Actor, Props}

import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}
import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.parameter.{PSMatrix, PSVector}
import com.sharkdtu.mlwheel.util.Utils

/**
 * The entrance interface for users.
 * Through [[PSClient]], users can create vectors or matrices.
 * All operations will be executed on parameter servers.
 *
 * @param conf The configuration specified by user
 */
class PSClient(conf: PSConf) {

  /**
   * Create a [[PSClient]] that loads settings from system properties.
   */
  def this() = this(new PSConf())

  /**
   * Create a [[PSClient]] that connects specified master address.
   */
  def this(masterHost: String, masterPort: Int) = this(
      new PSConf()
      .set(PS_MASTER_HOST, masterHost)
      .set(PS_MASTER_PORT, masterPort)
    )

  // Init PSContext.
  import com.sharkdtu.mlwheel.PSContext.Role._
  PSContext.create(conf, Utils.localHostname, CLIENT)

  private val actor = PSContext.get.actorSystem.actorOf(Props[PSClientActor])

  // ============================== //
  // Functions for creating vectors //
  // ============================== //
  /**
   * Create a zero [[PSVector]].
   *
   * @param numDimensions The number of vector's dimensions
   * @return The zero [[PSVector]] instance
   */
  def zeroVector[T](numDimensions: Int): PSVector[T] = ???

  /**
   * Create a random [[PSVector]], the random distribution is uniform.
   *
   * @param min The minimum of uniform distribution
   * @param max The maximum of uniform distribution
   * @return The random uniform [[PSVector]]
   */
  def randomUniformVector[T](
      numDimensions: Int,
      min: T,
      max: T): PSVector[T] = ???

  /**
   * Create a random [[PSVector]], the random distribution is normal.
   *
   * @param mean The mean parameter of uniform distribution
   * @param stddev The stddev parameter of uniform distribution
   * @return The random normal [[PSVector]]
   */
  def randomNormalVector[T](
      numDimensions: Int,
      mean: Double,
      stddev: Double): PSVector[T] = ???

  // =============================== //
  // Functions for creating matrices //
  // =============================== //
  /**
   * Create a zero [[PSMatrix]].
   *
   * @param numRows The number of rows
   * @param numCols The number of columns
   * @return The zero [[PSMatrix]] instance
   */
  def zeroMatrix[T](numRows: Int, numCols: Int): PSMatrix[T] = ???

}

/**
 * Companion Object for using singleton [[PSClient]]
 */
object PSClient {

  /**
   * Lock that guards creating `_client`.
   */
  private val _LOCK = new Object()

  /**
   * The singleton [[PSClient]] object.
   */
  private var _client: PSClient = _

  /**
   * This function may be used to get or instantiate a [[PSClient]] and register it as a
   * singleton object.
   *
   * @return The singleton [[PSClient]]
   *         (or a new one if it wasn't created before the function call)
   */
  def getOrCreate(): PSClient = {
    if (_client == null) {
      _LOCK.synchronized {
        if (_client == null) _client = new PSClient()
      }
    }
    _client
  }

  /**
   * This function may be used to get or instantiate a [[PSClient]] and register it as a
   * singleton object.
   *
   * @param conf The configuration specified by user
   * @return The singleton [[PSClient]]
   *         (or a new one if it wasn't created before the function call)
   */
  def getOrCreate(conf: PSConf): PSClient = {
    if (_client == null) {
      _LOCK.synchronized {
        if (_client == null) _client = new PSClient(conf)
      }
    }
    _client
  }

  /**
   * This function may be used to get or instantiate a [[PSClient]] and register it as a
   * singleton object.
   *
   * @param masterHost The master host
   * @param masterPort The master port
   * @return The singleton [[PSClient]]
   *         (or a new one if it wasn't created before the function call)
   */
  def getOrCreate(masterHost: String, masterPort: Int): PSClient = {
    if (_client == null) {
      _LOCK.synchronized {
        if (_client == null) _client = new PSClient(masterHost, masterPort)
      }
    }
    _client
  }
}

private class PSClientActor extends Actor with ActorLogReceive with Logging {
  override def receiveWithLogging: Receive = {
    case msg => logInfo(s"PSClient actor received message $msg")
  }
}