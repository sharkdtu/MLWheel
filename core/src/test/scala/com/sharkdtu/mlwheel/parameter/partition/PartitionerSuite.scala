package com.sharkdtu.mlwheel.parameter.partition

import com.sharkdtu.mlwheel.MLWheelFunSuite

class PartitionerSuite extends MLWheelFunSuite {

  test("RangePartitioner") {
    val partitioner = new RangePartitioner(4, 19)
    assert(partitioner.getPartitionId(9) == 2)
    assert(partitioner.partitions.length == 4)
    assert(partitioner.getPartitionId(1) == 0)
    assert(partitioner.getPartitionId(18) == 3)
    assert(partitioner.getPartitionId(7) == 1)
    assert(
      partitioner.partitions.foldLeft(0L) { (sum, p) =>
        sum + p.numElements
      } == 19
    )
  }

  // Test invalid elemIndex
  intercept[IndexOutOfBoundsException] {
    val partitioner = new RangePartitioner(4, 19)
    partitioner.getPartitionId(19)
  }

  // Test invalid elemIndex
  intercept[IndexOutOfBoundsException] {
    val partitioner = new RangePartitioner(4, 19)
    partitioner.getPartitionId(-1)
  }

}
