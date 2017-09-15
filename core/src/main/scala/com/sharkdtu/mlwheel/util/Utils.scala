package com.sharkdtu.mlwheel.util

import java.net.{BindException, Inet4Address, InetAddress, NetworkInterface}

import scala.collection.JavaConversions._

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.conf._
import com.sharkdtu.mlwheel.conf.PSConf
import com.sharkdtu.mlwheel.exception.PSException

/**
 * Various utility methods.
 */
private[mlwheel] object Utils extends Logging {

  /**
   * Attempt to start a service on the given port, or fail after a number of attempts.
   * Each subsequent attempt uses 1 + the port used in the previous attempt (unless the port is 0).
   *
   * @param startPort    The initial port to start the service on.
   * @param startService Function to start service on a given port.
   *                     This is expected to throw java.net.BindException on port collision.
   * @param conf         A [[PSConf]] used to get the maximum number of retries when binding to a port.
   * @param serviceName  Name of the service.
   * @return (service, port)
   */
  def startServiceOnPort[T](
      startPort: Int,
      startService: Int => (T, Int),
      conf: PSConf,
      serviceName: String = ""): (T, Int) = {

    require(startPort == 0 || (1024 <= startPort && startPort < 65536),
      "startPort should be between 1024 and 65535 (inclusive), or 0 for a random free port.")

    val serviceString = if (serviceName.isEmpty) "" else s" '$serviceName'"
    val maxRetries = portMaxRetries(conf)
    for (i <- 0 to maxRetries) {
      // Do not increment port if startPort is 0, which is treated as a special port
      val tryPort = if (startPort == 0) {
        startPort
      } else {
        // If the new port wraps around, do not try a privilege port
        ((startPort + i - 1024) % (65536 - 1024)) + 1024
      }
      try {
        val (service, port) = startService(tryPort)
        logInfo(s"Successfully started service$serviceString on port $port.")
        return (service, port)
      } catch {
        case e: Exception if isBindCollision(e) =>
          if (i >= maxRetries) {
            val exceptionMessage =
              s"${e.getMessage}: Service$serviceString failed after $maxRetries retries!"
            val exception = new BindException(exceptionMessage)
            // restore original stack trace
            exception.setStackTrace(e.getStackTrace)
            throw exception
          }
          logWarning(s"Service$serviceString could not bind on port $tryPort. " +
            s"Attempting port ${tryPort + 1}.")
      }
    }
    // Should never happen
    throw new PSException(s"Failed to start service$serviceString on port $startPort")
  }

  /**
   * Maximum number of retries when binding to a port before giving up.
   */
  def portMaxRetries(conf: PSConf): Int = {
    var maxRetries = conf.get(PS_PORT_MAX_RETRIES)
    if (conf.contains("ps.testing")) {
      // Set a higher number of retries for tests...
      maxRetries = 100
    }
    maxRetries
  }

  /**
   * Return whether the exception is caused by an address-port collision when binding.
   */
  def isBindCollision(exception: Throwable): Boolean = {
    exception match {
      case e: BindException =>
        if (e.getMessage != null) {
          return true
        }
        isBindCollision(e.getCause)
      case e: Exception => isBindCollision(e.getCause)
      case _ => false
    }
  }

  lazy val localIpAddress: String = findLocalIpAddress()
  lazy val localHostname: String = getAddressHostName(localIpAddress)

  def getAddressHostName(address: String): String = {
    InetAddress.getByName(address).getHostName
  }

  private def findLocalIpAddress(): String = {
    val address = InetAddress.getLocalHost
    if (address.isLoopbackAddress) {
      // Address resolves to something like 127.0.1.1, which happens on Debian; try to find
      // a better address using the local network interfaces
      // getNetworkInterfaces returns ifs in reverse order compared to ifconfig output order
      // on unix-like system. On windows, it returns in index order.
      // It's more proper to pick ip address following system output order.
      val activeNetworkIFs = NetworkInterface.getNetworkInterfaces.toList.reverse
      for (ni <- activeNetworkIFs) {
        for (addr <- ni.getInetAddresses if !addr.isLinkLocalAddress &&
             !addr.isLoopbackAddress && addr.isInstanceOf[Inet4Address]) {
          // We've found an address that looks reasonable!
          logWarning("Your hostname, " + InetAddress.getLocalHost.getHostName + " resolves to" +
            " a loopback address: " + address.getHostAddress + "; using " + addr.getHostAddress +
            " instead (on interface " + ni.getName + ")")
          return addr.getHostAddress
        }
      }
      logWarning("Your hostname, " + InetAddress.getLocalHost.getHostName + " resolves to" +
        " a loopback address: " + address.getHostAddress + ", but we couldn't find any" +
        " external IP address!")
    }
    address.getHostAddress
  }

}
