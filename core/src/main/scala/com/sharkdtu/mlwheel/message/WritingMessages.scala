package com.sharkdtu.mlwheel.message

/**
 * PS variable writing messages.
 */
private[mlwheel] object WritingMessages {

  case class CreateVectorRequest(
      clientId: String,
      numDimensions: Int,
      numPartitions: Int,
      partitionMode: Int, // 0 is Range, 1 is Hash.
      genFunc: () => Double) extends Request

  case class CreateVectorResponse(
      psVectorId: String,
      errorMsg: String) extends Response

}
