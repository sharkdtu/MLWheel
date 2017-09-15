package com.sharkdtu.mlwheel.parameter

import scala.reflect.{ClassTag, classTag}

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.parameter.partition.{Partition, Partitioner, RangePartitioner}

/**
 * A Variable reference represent a variable on ps.
 *
 * @param id The unique id of the variable
 */
abstract class PSVariable[T: ClassTag](val id: Int, val numPartitions: Int)
  extends Serializable with Logging {

  requireElemTypes()

  /**
   * A Optional name of this PSVariable
   */
  @transient private var _name: String = _

  /**
   * Specify how this PSVariable is partitioned.
   */
  @transient private var _partitioner: Partitioner = _

  /** Assign a name to this Variable */
  def setName(name: String): this.type = {
    _name = name
    this
  }

  def name: String = _name

  /**
   * Assign a partitioner to this Variable.
   *
   * @param partitioner The partitioner
   * @note It's not thread-safety
   */
  def setPartitioner(partitioner: Partitioner): this.type = {
    require(partitioner != null, "partitioner is null")
    _partitioner = partitioner
    this
  }

  def partitioner: Partitioner = _partitioner

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
  final def getPartitions: Array[Partition] = {
    if (partitioner == null) {
      // Set RangePartitioner to default partitioner
      setPartitioner(new RangePartitioner(numPartitions, numElements))
    }
    partitioner.partitions
  }

  /**
   * Returns the number of partitions of this variable.
   */
  final def getNumPartitions: Int = getPartitions.length

  /**
   * Get the values of this variable from ps.
   *
   * @return The values of this variable
   */
  def getValues: Array[T]

  /**
   * Get the specified partition values of this variable from ps.
   *
   * @param partitionId The partition index
   * @return The values of this variable
   */
  def getValues(partitionId: Int): Array[T]

  /**
   * Whether the `T` is one of type from "float, double".
   */
  private def requireElemTypes(): Unit = {
    val optionElemTypes = Array("float", "double")
    if(!optionElemTypes.exists(_.equals(classTag[T].runtimeClass.toString))){
      throw new UnsupportedOperationException(
        "The general type 'T' is limited in [Float, Double]")
    }
  }

  override def toString: String = "%s%s-%d".format(
    Option(name).map(_ + "-").getOrElse(""), getClass.getSimpleName, id)

}

