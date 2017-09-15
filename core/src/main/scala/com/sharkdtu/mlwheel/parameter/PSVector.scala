package com.sharkdtu.mlwheel.parameter

import scala.reflect.ClassTag

/**
 * A PS PSVector whose data is located on parameter servers.
 * It can not be instantiated through `new PSVector`.
 *
 * @param id The unique id of the vector
 */
class PSVector[@specialized(Float, Double) T: ClassTag] private(
    id: Int,
    numPartitions: Int,
    val numDimensions: Int
  ) extends PSVariable[T](id, numPartitions) {

  /**
   * Get the number of elements in this vector.
   *
   * @return The number of elements in this vector
   */
  override def numElements: Long = numDimensions.toLong

  /**
   * Get all the values of this vector from ps
   *
   * @return The values of this vector
   * @note This operation is expensive
   */
  override def getValues: Array[T] = ???

  /**
   * Get the specified partition values of this vector from ps
   *
   * @param partitionId The partition index
   * @return The values of this vector
   */
  override def getValues(partitionId: Int): Array[T] = ???

}

/**
 * The factory of PSVector, it is invisible to users.
 * The only entrance for users is [[com.sharkdtu.mlwheel.client.PSClient]]
 */
private[mlwheel] object PSVector {
  def apply[T: ClassTag](
      id: Int,
      numPartitions: Int,
      numDimensions: Int): PSVector[T] =
    new PSVector[T](id, numPartitions, numDimensions)
}
