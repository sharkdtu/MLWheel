package com.sharkdtu.mlwheel.parameter
import com.sharkdtu.mlwheel.parameter.partition.{Partition, RangePartitioner}

import scala.reflect.ClassTag

/**
 * A PS Matrix whose data is located on parameter servers.
 * It can not be instantiated through `new PSMatrix`.
 *
 * @param id The unique id of the matrix
 */
class PSMatrix[@specialized(Double) T: ClassTag] private(
    id: Int,
    numPartitions: Int,
    val numRows: Int,
    val numCols: Int
  ) extends PSVariable[T](id, numPartitions) {

  /**
   * Get the number of elements in this matrix.
   *
   * @return The number of elements in this matrix
   */
  override def numElements: Long = 1L * numRows * numCols

  /**
   * Get all partitions of this matrix
   *
   * @return All partitions
   */
  override def getPartitions: Array[Partition] = {
    // Default partitioner is RangePartitioner
    partitioner.getOrElse(
      new RangePartitioner(numPartitions, numElements)
    ).partitions
  }

  /**
   * Get all the values of this matrix from ps.
   *
   * @return The values of this matrix
   * @note This operation is expensive
   */
  override def getValues: Array[T] = ???

  /**
   * Get the specified partition values of this matrix from ps
   *
   * @param partitionId The partition index
   * @return The values of this matrix
   */
  override def getValues(partitionId: Int): Array[T] = ???
}

/**
 * The factory of PSMatrix, it is invisible to users
 */
private[mlwheel] object PSMatrix {
  def apply[T: ClassTag](
      id: Int,
      numPartitions: Int,
      numRows: Int,
      numCols: Int): PSMatrix[T] =
    new PSMatrix[T](id, numPartitions, numRows, numCols)
}
