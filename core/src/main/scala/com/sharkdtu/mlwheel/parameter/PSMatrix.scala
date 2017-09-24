package com.sharkdtu.mlwheel.parameter

import com.sharkdtu.mlwheel.client.PSClient
import com.sharkdtu.mlwheel.parameter.partition.Partitioner.PartitionMode._

/**
 * A PS Matrix whose data is located on parameter servers.
 * It can not be instantiated through `new PSMatrix`.
 *
 * @param id The unique id
 * @param numPartitions The number of partitions
 * @param partitionMode The partition mode
 * @param numRows The number of rows
 * @param numCols The number of cols
 * @param client The client who create this matrix
 */
class PSMatrix private[mlwheel](
    id: String,
    numPartitions: Int,
    partitionMode: PartitionMode,
    val numRows: Int,
    val numCols: Int,
    client: PSClient
  ) extends PSVariable(id, numPartitions, partitionMode, client) {

  /**
   * Get the number of elements in this matrix.
   *
   * @return The number of elements in this matrix
   */
  override def numElements: Long = 1L * numRows * numCols

  /**
   * Get all the values of this matrix from ps.
   *
   * @return The values of this matrix
   * @note This operation is expensive
   */
  override def getValues: Array[Double] = ???

  /**
   * Get the specified partition values of this matrix from ps
   *
   * @param partitionId The partition index
   * @return The values of this matrix
   */
  override def getValues(partitionId: Int): Array[Double] = ???
}