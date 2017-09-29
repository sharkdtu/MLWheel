package com.sharkdtu.mlwheel.parameter

import com.sharkdtu.mlwheel.client.PSClient
import com.sharkdtu.mlwheel.parameter.partition.Partition

/**
 * A PS Matrix whose data is located on parameter servers.
 * It can only be instantiated through [[PSClient]].
 *
 * @param id The unique id
 * @param partitions The partitions
 * @param numRows The number of rows
 * @param numCols The number of cols
 */
class PSMatrix private[mlwheel](
    id: String,
    partitions: Array[Partition],
    val numRows: Int,
    val numCols: Int
  ) extends PSVariable(id, partitions) {

  require(1L * numRows * numCols == numElements,
    "The sum of all the partitions is not equal 'numRows * numCols'.")

  /**
   * Get iterator of this matrix values.
   *
   * @return The values of this matrix
   * @note This operation is expensive
   */
  override def iterator: Iterator[Double] = ???

  /**
   * Get the specified partition values of this matrix from ps
   *
   * @param partitionId The partition index
   * @return The values of this matrix
   */
  override def get(partitionId: Int): Array[Double] = ???
}