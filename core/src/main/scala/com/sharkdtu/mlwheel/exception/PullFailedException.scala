package com.sharkdtu.mlwheel.exception

/**
 * An exception that occurs when a pull fails
 *
 * @param message A specific error message detailing what failed
 */
class PullFailedException(message: String) extends MLWheelException(message)
