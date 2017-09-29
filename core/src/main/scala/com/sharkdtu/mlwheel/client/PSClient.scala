package com.sharkdtu.mlwheel.client

import scala.util.Random

import akka.actor.{Actor, Props}

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.exception.PSException
import com.sharkdtu.mlwheel.message.RegisterMessages.{RegisterClientFailed, RegisterClientRequest, RegisterClientResponse, RegisteredClient}
import com.sharkdtu.mlwheel.message.WritingMessages.{CreateVectorRequest, CreateVectorResponse}
import com.sharkdtu.mlwheel.parameter.partition.Partitioner._
import com.sharkdtu.mlwheel.parameter.{PSMatrix, PSVector}
import com.sharkdtu.mlwheel.util.{AkkaUtils, ClosureUtils, Utils}
import com.sharkdtu.mlwheel.{ActorLogReceive, Logging, PSContext}

/**
 * The entrance interface for users.
 * Through [[PSClient]], users can create vectors or matrices.
 * All operations will be executed on parameter servers.
 *
 * @param conf The configuration specified by user
 */
class PSClient(conf: PSConf) extends Logging {

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

  // Init PSContext first.
  import com.sharkdtu.mlwheel.PSContext.Role._
  PSContext.create(conf, Utils.localHostname, CLIENT)

  // The PSMasterActor reference for sending RPC messages.
  private val master = AkkaUtils.makePSMasterRef(
    PSContext.PSMasterActorNames.psMasterActorName, conf, actorSystem)

  // The PSClientActor reference as unique id of this PSClient.
  private val client = actorSystem.actorOf(Props[PSClientActor])

  // The unique id of this client.
  private val id = s"${AkkaUtils.getHostPort(actorSystem)}#${AkkaUtils.getUid(client)}"

  // Register to master
  private val registerResponse = AkkaUtils.askWithRetry[RegisterClientResponse](
    master, RegisterClientRequest(client), conf)

  registerResponse match {
    case RegisterClientFailed(errorMsg) =>
      throw new PSException(s"Register failed: $errorMsg")
    case RegisteredClient =>
      logInfo("Register to master successfully.")
  }

  private def actorSystem = PSContext.get.actorSystem

  // ============================== //
  // Functions for creating vectors //
  // ============================== //
  /**
   * Create a zero [[PSVector]].
   *
   * @param numDimensions The number of vector's dimensions
   * @param numPartitions The number of partitions, default 0 means auto split
   * @param partitionMode The partition mode: `RANGE` or `HASH`
   * @return The zero [[PSVector]] instance
   */
  def zeroVector(
      numDimensions: Int,
      numPartitions: Int = 0,
      partitionMode: Byte = RANGE): PSVector = {
    createVector(numDimensions, numPartitions, partitionMode, () => 0.0)
  }

  /**
   * Create a random [[PSVector]], the random distribution is uniform.
   *
   * @param numDimensions The number of vector's dimensions
   * @param min The minimum of uniform distribution
   * @param max The maximum of uniform distribution
   * @param numPartitions The number of partitions, default 0 means auto split
   * @param partitionMode The partition mode: `RANGE` or `HASH`
   * @return The random uniform [[PSVector]]
   */
  def randomUniformVector(
      numDimensions: Int,
      min: Double,
      max: Double,
      numPartitions: Int = 0,
      partitionMode: Byte = RANGE,
      seed: Long = System.currentTimeMillis()): PSVector = {
    def genFunc(): Double = {
      val rand = new Random(seed)
      (max - min) * rand.nextDouble() + min
    }

    createVector(numDimensions, numPartitions, partitionMode, genFunc)
  }

  /**
   * Create a random [[PSVector]], the random distribution is normal.
   *
   * @param numDimensions The number of vector's dimensions
   * @param mean The mean parameter of uniform distribution
   * @param stddev The stddev parameter of uniform distribution
   * @param numPartitions The number of partitions, default 0 means auto split
   * @param partitionMode The partition mode: `RANGE` or `HASH`
   * @return The random normal [[PSVector]]
   */
  def randomNormalVector(
      numDimensions: Int,
      mean: Double,
      stddev: Double,
      numPartitions: Int = 0,
      partitionMode: Byte = RANGE,
      seed: Long = System.currentTimeMillis()): PSVector = {
    def genFunc(): Double = {
      val rand = new Random(seed)
      stddev * rand.nextGaussian() + mean
    }

    createVector(numDimensions, numPartitions, partitionMode, genFunc)
  }

  private def createVector(
      numDimensions: Int,
      numPartitions: Int,
      partitionMode: Byte,
      genFunc: () => Double): PSVector = {
    val actualNumPartitions = Utils.getActualNumPartitions(
      numDimensions, numPartitions, conf)
    val cleanedFunc = clean(genFunc)
    val resp = AkkaUtils.askWithRetry[CreateVectorResponse](master, CreateVectorRequest(id,
      numDimensions, actualNumPartitions, partitionMode, cleanedFunc), conf)
    if (resp.psVectorId == PSContext.PS_VARIABLE_FAKE_ID) {
      throw new PSException(s"Create ps vector failed: ${resp.errorMsg}")
    }
    logInfo(s"Successfully create ps vector(${resp.psVectorId})")
    new PSVector(resp.psVectorId, actualNumPartitions, partitionMode, numDimensions, this)
  }

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
  def zeroMatrix(numRows: Int, numCols: Int): PSMatrix = null  // TODO

  /**
   * Clean a closure to make it ready to serialized and send to master/worker
   * (removes unreferenced variables in $outer's, updates REPL variables)
   * If `checkSerializable` is set, `clean` will also proactively
   * check to see if `f` is serializable and throw a `PSException` if not.
   *
   * @param f The closure to clean
   * @param checkSerializable Whether or not to immediately check `f` for serializability
   * @throws `PSException` if `checkSerializable` is set but `f` is not serializable
   */
  private def clean[F <: AnyRef](f: F, checkSerializable: Boolean = true): F = {
    ClosureUtils.clean(f, checkSerializable)
    f
  }

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
    case msg => logInfo(s"PSClient($self) received message: $msg")
  }
}