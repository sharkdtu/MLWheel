package com.sharkdtu.mlwheel.message

/**
 * PS variable writing messages.
 */
private[mlwheel] object WritingMessages {

  case class CreateVectorRequest(
      clientId: String,
      numDimensions: Int,
      numPartitions: Int,
      partitionMode: Byte, // 0: Range, 1: Hash.
      genFunc: () => Double) extends RpcRequest

  case class CreateVectorResponse(
      psVectorId: String,
      errorMsg: String) extends RpcResponse

}
