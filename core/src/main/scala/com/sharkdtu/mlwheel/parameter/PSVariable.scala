package com.sharkdtu.mlwheel.parameter

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.parameter.partition.{Partition, Partitioner}

import scala.reflect.{classTag, ClassTag}

/**
 * A Variable reference represent a variable on ps.
 *
 * @param id The unique id of the variable
 */
abstract class PSVariable[T: ClassTag](val id: Int, val numPartitions: Int)
  extends Serializable with Logging {

  requireType()

  @transient
  private var _name: String = _

  /**
   * Optionally set by subclasses to specify how they are partitioned.
   */
  @transient var _partitioner: Option[Partitioner] = None

  /** Assign a name to this Variable */
  def setName(name: String): this.type = {
    _name = name
    this
  }

  def name: String = _name

  /**
   * Assign a partitioner to this Variable
   */
  def setPartitioner(partitioner: Partitioner): this.type = {
    require(partitioner != null, "partitioner is null")
    _partitioner = Some(partitioner)
    this
  }

  def partitioner: Option[Partitioner] = _partitioner

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
  def getPartitions: Array[Partition]

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
   * Check the T is one of type from "int, long, float, double".
   */
  private def requireType(): Unit = {
    val optionElemTypes = Array("int", "long", "float", "double")
    if(!optionElemTypes.exists(_.equals(classTag[T].runtimeClass.toString))){
      throw new UnsupportedOperationException(
        "The general type 'T' is limited in [Int, Long, Float, Double]")
    }
  }

  override def toString: String = "%s%s-%d".format(
    Option(name).map(_ + "-").getOrElse(""), getClass.getSimpleName, id)

}

