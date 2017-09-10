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

}

/**
 * Companion Object for using singleton client
 */
object MLWheelClient {

  /**
   * Lock that guards creating `_client`.
   */
  private val _LOCK = new Object()

  /**
   * The singleton MLWheelClient object.
   */
  private var _client: MLWheelClient = _

  /**
   * This function may be used to get or instantiate a MLWheelClient and register it as a
   * singleton object.
   *
   * @return The singleton `MLWheelClient` (or a new one if wasn't created before the function call)
   */
  def getOrCreate(): MLWheelClient = {
    if (_client == null) {
      _LOCK.synchronized {
        if (_client == null) _client = new MLWheelClient()
      }
    }
    _client
  }

  /**
   * This function may be used to get or instantiate a MLWheelClient and register it as a
   * singleton object.
   *
   * @param conf The MLWheel configuration
   * @return The singleton `MLWheelClient` (or a new one if wasn't created before the function call)
   */
  def getOrCreate(conf: MLWheelConf): MLWheelClient = {
    if (_client == null) {
      _LOCK.synchronized {
        if (_client == null) _client = new MLWheelClient(conf)
      }
    }
    _client
  }
}
