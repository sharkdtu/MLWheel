package com.sharkdtu.mlwheel.parameter

/**
 * A PS Matrix whose data is located on parameter servers.
 * It can not be instantiated through `new PSMatrix`.
 *
 * @param id The unique id of the matrix
 */
class PSMatrix private(
    id: String,
    numPartitions: Int,
    val numRows: Int,
    val numCols: Int
  ) extends PSVariable(id, numPartitions) {

  /**
   * Get the number of elements in this matrix.
   *
   * @return The number of elements in this matrix
   */
  override def numElements: Long = 1L * numRows * numCols

  /**
   * Get all the values of this matrix from ps.
   *
   * @return The values of this matrix
   * @note This operation is expensive
   */
  override def getValues: Array[Double] = ???

  /**
   * Get the specified partition values of this matrix from ps
   *
   * @param partitionId The partition index
   * @return The values of this matrix
   */
  override def getValues(partitionId: Int): Array[Double] = ???
}

/**
 * The factory of PSMatrix, it is invisible to users.
 * The only entrance for users is [[com.sharkdtu.mlwheel.client.PSClient]]
 */
private[mlwheel] object PSMatrix {
  def apply(
      id: Int,
      numPartitions: Int,
      numRows: Int,
      numCols: Int): PSMatrix =
    new PSMatrix(id, numPartitions, numRows, numCols)
}
