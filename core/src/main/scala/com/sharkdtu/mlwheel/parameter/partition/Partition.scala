package com.sharkdtu.mlwheel.parameter.partition

import akka.actor.ActorRef

/**
 * An abstract partition.
 *
 * @param index The index of this partition
 * @param worker The location of this partition, for RPC
 */
abstract class Partition(val index: Int, val worker: ActorRef) extends Serializable {

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
 * @param start The start index in variable
 * @param end The end index in variable
 */
class RangePartition(index: Int, worker: ActorRef, start: Long, end: Long)
  extends Partition(index, worker) {

  /**
   * Get the number of elements in this partition.
   *
   * @return The number of elements in this partition
   */
  override def numElements: Long = end - start
}
