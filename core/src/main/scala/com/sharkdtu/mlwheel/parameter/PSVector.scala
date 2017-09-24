package com.sharkdtu.mlwheel.parameter

import com.sharkdtu.mlwheel.client.PSClient
import com.sharkdtu.mlwheel.parameter.partition.Partitioner.PartitionMode._

/**
 * A PS Vector whose data is located on parameter servers.
 * It can not be instantiated through `new PSVector`.
 *
 * @param id The unique id
 * @param numPartitions The number of partitions
 * @param partitionMode The partition mode
 * @param numDimensions The number of dimensions
 * @param client The client who create this vector
 */
class PSVector private[mlwheel](
    id: String,
    numPartitions: Int,
    partitionMode: PartitionMode,
    val numDimensions: Int,
    client: PSClient
  ) extends PSVariable(id, numPartitions, partitionMode, client) {

import com.sharkdtu.mlwheel.client.PSClient

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
  override def getValues: Array[Double] = ???

  /**
   * Get the specified partition values of this vector from ps
   *
   * @param partitionId The partition index
   * @return The values of this vector
   */
  override def getValues(partitionId: Int): Array[Double] = ???

}