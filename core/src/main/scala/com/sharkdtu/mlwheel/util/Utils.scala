package com.sharkdtu.mlwheel.util

import java.io._
import java.net.{BindException, Inet4Address, InetAddress, NetworkInterface}
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConversions._
import scala.util.control.NonFatal

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

  def getActualNumPartitions(numDimensions: Int, numPartitions: Int, conf: PSConf): Int = {
    val minNumElementsOfPartition = conf.get(PARTITION_MIN_ELEMENTS)
    val maxNumElementsOfPartition = conf.get(PARTITION_MAX_ELEMENTS)
    if (numPartitions <= 0) {
      logInfo("numPartitions <= 0, auto split the vector.")
      1 + numDimensions / minNumElementsOfPartition
    } else {
      val numElementsOfLargePartition = 1 + numDimensions / numPartitions
      if (numElementsOfLargePartition > maxNumElementsOfPartition) {
        logWarning(s"The number elements of one partition maybe greater than" +
          s"${PARTITION_MAX_ELEMENTS.key}($maxNumElementsOfPartition)")
        numPartitions
      } else if (numElementsOfLargePartition < minNumElementsOfPartition) {
        logWarning(s"The number elements of one partition maybe less than" +
          s"${PARTITION_MIN_ELEMENTS.key}($minNumElementsOfPartition), auto split the vector.")
        1 + numDimensions / minNumElementsOfPartition
      } else {
        numPartitions
      }
    }
  }

  /**
   * Copy all data from an InputStream to an OutputStream. NIO way of file stream to file stream
   * copying is disabled by default unless explicitly set transferToEnabled as true.
   */
  def copyStream(
      in: InputStream,
      out: OutputStream,
      closeStreams: Boolean = false,
      transferToEnabled: Boolean = false): Long = {
    var count = 0L
    try {
      if (in.isInstanceOf[FileInputStream] && out.isInstanceOf[FileOutputStream]
        && transferToEnabled) {
        // When both streams are File stream, use transferTo to improve copy performance.
        val inChannel = in.asInstanceOf[FileInputStream].getChannel
        val outChannel = out.asInstanceOf[FileOutputStream].getChannel
        val initialPos = outChannel.position()
        val size = inChannel.size()

        // In case transferTo method transferred less data than we have required.
        while (count < size) {
          count += inChannel.transferTo(count, size - count, outChannel)
        }

        // Check the position after transferTo loop to see if it is in the right position and
        // give user information if not.
        // Position will not be increased to the expected length after calling transferTo in
        // kernel version 2.6.32, this issue can be seen in
        // https://bugs.openjdk.java.net/browse/JDK-7052359
        val finalPos = outChannel.position()
        assert(finalPos == initialPos + size,
          s"""
             |Current position $finalPos do not equal to expected position ${initialPos + size}
             |after transferTo, please check your kernel version to see if it is 2.6.32,
             |this is a kernel bug which will lead to unexpected behavior when using transferTo.
             |You can set ps.file.transferTo = false to disable this NIO feature.
           """.stripMargin)
      } else {
        val buf = new Array[Byte](8192)
        var n = 0
        while (n != -1) {
          n = in.read(buf)
          if (n != -1) {
            out.write(buf, 0, n)
            count += n
          }
        }
      }
      count
    } finally {
      if (closeStreams) {
        try {
          in.close()
        } finally {
          out.close()
        }
      }
    }
  }

  /**
   * Execute a block of code that evaluates to Unit, re-throwing any non-fatal uncaught
   * exceptions as IOException.  This is used when implementing Externalizable and Serializable's
   * read and write methods, since Java's serializer will not report non-IOExceptions properly;
   */
  def tryOrIOException(block: => Unit) {
    try {
      block
    } catch {
      case e: IOException => throw e
      case NonFatal(t) => throw new IOException(t)
    }
  }

  /**
   * Get the ClassLoader which loaded Spark.
   */
  def getPSClassLoader: ClassLoader = getClass.getClassLoader

  /**
   * Get the Context ClassLoader on this thread or, if not present, the ClassLoader that
   * loaded PS.
   *
   * This should be used whenever passing a ClassLoader to Class.ForName or finding the currently
   * active loader when setting up ClassLoader delegation chains.
   */
  def getContextOrPSClassLoader: ClassLoader =
    Option(Thread.currentThread().getContextClassLoader).getOrElse(getPSClassLoader)

  /**
   * Preferred alternative to Class.forName(className)
   */
  def classForName(className: String): Class[_] =
    Class.forName(className, true, getContextOrPSClassLoader)

  /**
   * Get an UUID
   */
  def getNextUUID: String = UUID.randomUUID().toString.replace("-", "")

}
