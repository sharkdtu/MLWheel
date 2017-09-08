package com.sharkdtu.mlwheel.client

import java.util.concurrent.atomic.AtomicReference

import com.sharkdtu.mlwheel.conf.MLWheelConf
import com.sharkdtu.mlwheel.parameter.{PSMatrix, PSVector}

/**
 * The entrance for using MLWheel.
 * Through [[MLWheelClient]], users can create/delete vectors or matrices.
 * All operations will be executed on parameter servers.
 *
 * @param conf The configuration specified by user
 */
class MLWheelClient(conf: MLWheelConf) {

  /**
   * Create a MLWheelClient that loads settings from system properties.
   */
  def this() = this(new MLWheelConf())

  /**
   * Create a zero PSVector.
   *
   * @param numDimensions The number of vector's dimensions
   * @return The zero `PSVector` instance
   */
  def zeroVector[T](numDimensions: Int): PSVector[T] = ???

  /**
   * Create a zero PSMatrix.
   *
   * @param numRows The number of rows
   * @param numCols The number of columns
   * @return The zero `PSMatrix` instance
   */
  def zeroMatrix[T](numRows: Int, numCols: Int): PSMatrix[T] = ???

  /**
   * Create a random PSVector, the random distribution is uniform.
   *
   * @param min The minimum of uniform distribution
   * @param max The maximum of uniform distribution
   * @return The random uniform PSVector
   */
  def randomUniformVector[T](
      numDimensions: Int,
      min: Double,
      max: Double): PSVector[T] = ???

  /**
   * Create a random PSVector, the random distribution is normal.
   *
   * @param mean The mean parameter of uniform distribution
   * @param stddev The stddev parameter of uniform distribution
   * @return The random normal PSVector
   */
  def randomNormalVector[T](
      numDimensions: Int,
      mean: Double,
      stddev: Double): PSVector[T] = ???

  // Mark this client as last active client.
  // NOTE: this must be placed at the end of the MLWheelClient constructor.
  MLWheelClient.setLastActiveClient(this)
}

object MLWheelClient {

  /**
   * Lock that guards access to global `_lastActiveClient`.
   */
  private val _LOCK = new Object()

  /**
   * The last active, fully-constructed MLWheelClient.
   * If no MLWheelClient is active, then this is `null`.
   *
   * Access to this field is guarded by _LOCK.
   */
  private var _lastActiveClient: AtomicReference[MLWheelClient] =
    new AtomicReference[MLWheelClient](null)

  /**
   * Set the last active client.
   * Called at the end of the MLWheelClient constructor.
   *
   * @param client The MLWheelClient object
   */
  private[mlwheel] def setLastActiveClient(client: MLWheelClient): Unit = {
    _LOCK.synchronized {
      _lastActiveClient.set(client)
    }
  }

  /**
   * This function may be used to get or instantiate a MLWheelClient and register it as a
   * latest object.
   *
   * @return Last active `MLWheelClient` (or a new one if wasn't created before the function call)
   */
  def getOrCreate(): MLWheelClient = {
    _LOCK.synchronized {
      if (_lastActiveClient.get() == null) {
        setLastActiveClient(new MLWheelClient())
      }
      _lastActiveClient.get()
    }
  }

  def getOrCreate(conf: MLWheelConf): MLWheelClient = {
    _LOCK.synchronized {
      if (_lastActiveClient.get() == null) {
        setLastActiveClient(new MLWheelClient(conf))
      }
      _lastActiveClient.get()
    }
  }
}
