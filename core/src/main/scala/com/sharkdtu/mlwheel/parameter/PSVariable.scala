package com.sharkdtu.mlwheel.parameter

import java.util.concurrent.locks.ReentrantReadWriteLock

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.parameter.partition._

/**
 * A Variable reference represent a variable(vector or matrix) on ps.
 *
 * @param id The unique id of the variable
 * @param partitions The partitions of the variable
 */
abstract class PSVariable(
    val id: String,
    val partitions: Array[Partition]
  ) extends Serializable with Logging {

  /**
   * A Optional name of this PSVariable
   */
  @transient private var _name: String = _

  /**
   * The read/write lock for this variable
   */
  @transient private val lock = new ReentrantReadWriteLock()

  /** Assign a name to this Variable */
  def setName(name: String): this.type = {
    _name = name
    this
  }

  def name: String = _name

//  /**
//   * Return the partitioner of this variable, or `RangePartitioner` if not set
//   */
//  lazy val partitioner: Partitioner = {
//    partitionMode match {
//      case RANGE =>
//        new RangePartitioner(numPartitions, numElements)
//      case HASH =>
//        throw new UnsupportedOperationException(s"Unsupport hash partitioner temporarily.")
//    }
//  }

  /**
   * Get the number of elements in this variable.
   *
   * @return The number of elements in this variable
   */
  final def numElements: Long = partitions.foldLeft(0L) {
    (left, part) => left + part.numElements
  }

//  /**
//   * Get all partitions of this variable.
//   *
//   * @return All partitions
//   */
//  final def getPartitions: Array[Partition] = partitioner.partitions

  /**
   * Get iterator of this variable values.
   *
   * @return The iterator values
   */
  def iterator: Iterator[Double]

  /**
   * Get the specified partition values of this variable from ps.
   *
   * @param partitionId The partition index
   * @return The values of this variable
   */
  def get(partitionId: Int): Array[Double]

  /**
   * return friendly string
   */
  override def toString: String = "%s%s-%s".format(
    Option(name).map(_ + "-").getOrElse(""), getClass.getSimpleName, id)

}

