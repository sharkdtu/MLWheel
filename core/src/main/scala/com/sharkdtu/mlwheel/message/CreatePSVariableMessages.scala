package com.sharkdtu.mlwheel.message

/**
 *
 */
private[mlwheel] object CreatePSVariableMessages {

  case class CreateVector(numDimensions: Int, numPartitions: Int, createFunc: () => Double)

}
