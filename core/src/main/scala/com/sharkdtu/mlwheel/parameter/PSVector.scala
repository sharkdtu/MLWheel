package com.sharkdtu.mlwheel.parameter

import com.sharkdtu.mlwheel.client.PSClient
import com.sharkdtu.mlwheel.parameter.partition.Partition

/**
 * A PS Vector whose data is located on parameter servers.
 * It can only be created through [[PSClient]].
 *
 * @param id The unique id
 * @param partitions The partitions
 */
class PSVector private[mlwheel](
    id: String,
    partitions: Array[Partition],
    val numDimensions: Int
  ) extends PSVariable(id, partitions) {

  require(numDimensions.toLong == numElements,
    "The sum of all the partitions is not equal numDimensions.")

  /**
   * Get iterator of this vector values.
   *
   * @return The values of this vector
   * @note This operation is expensive
   */
  override def iterator: Iterator[Double] = ???

  /**
   * Get the specified partition values of this vector from ps
   *
   * @param partitionId The partition index
   * @return The values of this vector
   */
  override def get(partitionId: Int): Array[Double] = ???
}