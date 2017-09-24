package com.sharkdtu.mlwheel.parameter.partition

/**
 * An abstract partitioner that defines how the elements
 * in [[com.sharkdtu.mlwheel.parameter.PSVariable]] are partitioned by element's index.
 * Maps each element's index to a partition id, from 0 to `numPartitions - 1`.
 */
abstract class Partitioner(val numPartitions: Int) {
  def partitions: Array[Partition]
  def getPartitionId(elemIndex: Long): Int
}

object Partitioner {
  object PartitionMode extends Enumeration {
    type PartitionMode = Value
    val RANGE, HASH = Value
  }
}

/**
 * A range partitioner that partition elements range by range
 *
 * @param numPartitions The number of partitions
 * @param numElements The number of elements
 */
class RangePartitioner(numPartitions: Int, numElements: Long)
  extends Partitioner(numPartitions) {

  /**
   * Example:
   * 19 elems and 4 partitions:
   *
   * xxxx   |   xxxx   |   xxxxx   |   xxxxx
   * \________________/ \____________________/
   *         \/                  \/
   * [left partitions]    [right partitions]
   */
  @transient
  override lazy val partitions: Array[Partition] = {
    val parts = new Array[Partition](numPartitions)

    val numElemsOfLeftPartitions = numElements / numPartitions
    val numElemsOfRightPartitions = numElemsOfLeftPartitions + 1
    val numRightPartitions = numElements % numPartitions
    val numLeftPartitions = numPartitions - numRightPartitions

    var i = 0
    var start = 0L

    while (i < numLeftPartitions) {
      parts(i) = new RangePartition(i, start, start + numElemsOfLeftPartitions)
      start += numElemsOfLeftPartitions
      i += 1
    }

    while (i < numPartitions) {
      parts(i) = new RangePartition(i, start, start + numElemsOfRightPartitions)
      start += numElemsOfRightPartitions
      i += 1
    }

    parts
  }

  /**
   * Get the partition id of the element's index
   *
   * @param elemIndex The element's index
   * @return The partition id
   */
  override def getPartitionId(elemIndex: Long): Int = {
    // elemIndex must be within range
    if (elemIndex < 0 || elemIndex >= numElements) {
      throw new IndexOutOfBoundsException(s"$elemIndex is not in [0, $numElements)")
    }

    val numElemsOfLeftPartitions = numElements / numPartitions
    val numElemsOfRightPartitions = numElemsOfLeftPartitions + 1
    val numRightPartitions = (numElements % numPartitions).toInt
    val numLeftPartitions = numPartitions - numRightPartitions

    if (elemIndex < numLeftPartitions * numElemsOfLeftPartitions) {
      (elemIndex / numElemsOfLeftPartitions).toInt
    } else {
      val relativeGap = elemIndex - numLeftPartitions * numElemsOfLeftPartitions
      numLeftPartitions + (relativeGap / numElemsOfRightPartitions).toInt
    }
  }

}
