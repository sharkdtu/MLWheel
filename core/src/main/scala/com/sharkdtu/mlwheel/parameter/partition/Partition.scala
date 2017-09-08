package com.sharkdtu.mlwheel.parameter.partition

/**
 * An abstract partition.
 *
 * @param index The index of this partition
 */
abstract class Partition(val index: Int) extends Serializable {

  /**
   * Get the number of elements in this partition.
   *
   * @return The number of elements in this partition
   */
  def numElements: Long

}

/**
 * A range partition, which represents the range indices "[start, end)" of PSVariable.
 *
 * @param index The index of this partition
 * @param start The start index of this partition
 * @param end The end index of this partition
 */
class RangePartition(index: Int, start: Long, end: Long) extends Partition(index) {

  /**
   * Get the number of elements in this partition.
   *
   * @return The number of elements in this partition
   */
  override def numElements: Long = end - start

}
