package com.sharkdtu.mlwheel.parameter

import java.util.concurrent.locks.ReentrantReadWriteLock

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.client.PSClient
import com.sharkdtu.mlwheel.parameter.partition._
import com.sharkdtu.mlwheel.parameter.partition.Partitioner.PartitionMode._

/**
 * A Variable reference represent a variable(vector or matrix) on ps.
 *
 * @param id The unique id of the variable
 * @param numPartitions The number of partitions
 * @param partitionMode The partition mode of this variable
 * @param client The client who create this variable
 */
abstract class PSVariable(
    val id: String,
    val numPartitions: Int,
    partitionMode: PartitionMode,
    client: PSClient) extends Logging {

  /**
   * A Optional name of this PSVariable
   */
  private var _name: String = _

  /**
   * The read/write lock for this variable
   */
  private val lock = new ReentrantReadWriteLock()

  /** Assign a name to this Variable */
  def setName(name: String): this.type = {
    _name = name
    this
  }

  def name: String = _name

  /**
   * Return the partitioner of this variable, or `RangePartitioner` if not set
   */
  def partitioner: Partitioner = {
    partitionMode match {
      case RANGE =>
        new RangePartitioner(numPartitions, numElements)
      case HASH =>
        throw new UnsupportedOperationException(s"Unsupport hash partitioner temporarily.")
    }
  }

  /**
   * Get the number of elements in this variable.
   *
   * @return The number of elements in this variable
   */
  def numElements: Long

  /**
   * Get all partitions of this variable.
   *
   * @return All partitions
   */
  final def getPartitions: Array[Partition] = partitioner.partitions

  /**
   * Get the values of this variable from ps.
   *
   * @return The values of this variable
   */
  def getValues: Array[Double]

  /**
   * Get the specified partition values of this variable from ps.
   *
   * @param partitionId The partition index
   * @return The values of this variable
   */
  def getValues(partitionId: Int): Array[Double]

  override def toString: String = "%s%s-%d".format(
    Option(name).map(_ + "-").getOrElse(""), getClass.getSimpleName, id)

}

