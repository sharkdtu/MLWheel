package com.sharkdtu.mlwheel.exception

/**
 * An internal exception of MLWheel
 *
 * @param message A specific error message detailing what failed
 */
class PSException(message: String, e: Throwable) extends Exception(message, e) {
  def this(message: String) = this(message, null)
}
