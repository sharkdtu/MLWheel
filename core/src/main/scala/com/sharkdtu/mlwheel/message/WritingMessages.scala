package com.sharkdtu.mlwheel.message

/**
 * PS variable writing messages.
 */
private[mlwheel] object WritingMessages {

  case class CreateVector(numDimensions: Int, numPartitions: Int, genFunc: () => Double)

}
