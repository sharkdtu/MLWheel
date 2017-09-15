package com.sharkdtu.mlwheel.util

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem}
import akka.pattern.ask

import com.typesafe.config.ConfigFactory

import com.sharkdtu.mlwheel.conf.{PSConf, _}
import com.sharkdtu.mlwheel.exception.PSException
import com.sharkdtu.mlwheel.{Logging, PSContext}

/**
 * Various utility methods for working with akka.
 */
private[mlwheel] object AkkaUtils extends Logging {

  /**
   * Creates an ActorSystem ready for remoting. Returns both the
   * ActorSystem itself and its port (which is hard to get from Akka).
   *
   * Note: the `name` parameter is important, as even if a client sends a message to right
   * host + port, if the system name is incorrect, Akka will drop the message.
   *
   * If indestructible is set to true, the Actor System will continue running in the event
   * of a fatal exception.
   */
  def createActorSystem(
      name: String,
      host: String,
      port: Int,
      conf: PSConf): (ActorSystem, Int) = {
    val startService: Int => (ActorSystem, Int) = { actualPort =>
      doCreateActorSystem(name, host, actualPort, conf)
    }
    Utils.startServiceOnPort(port, startService, conf, name)
  }

  private def doCreateActorSystem(
      name: String,
      host: String,
      port: Int,
      conf: PSConf): (ActorSystem, Int) = {
    val akkaNumThreads = conf.get(AKKA_NUM_THREADS)
    val akkaDispatcherThroughput = conf.get(AKKA_DISPATCHER_THROUGHPUT)
    val akkaConnectTimeoutSec = conf.get(AKKA_CONNECT_TIMEOUT)
    val akkaFrameSizeBytes = maxFrameSizeBytes(conf)
    val akkaLifecycleEvents = if (conf.get(AKKA_LOG_LIFT_CYCLE_EVENTS)) "on" else "off"
    val akkaLogConfig = if (conf.get(AKKA_LOG_CONFIG)) "on" else "off"
    val akkaHeartBeatPausesSec = conf.get(AKKA_HEARTBEAT_PAUSES)
    val akkaHeartBeatIntervalSec = conf.get(AKKA_HEARTBEAT_INTERVAL)

    val akkaConf = ConfigFactory.parseString(
      s"""
      |akka.daemonic = on
      |akka.loggers = [""akka.event.slf4j.Slf4jLogger""]
      |akka.stdout-loglevel = "ERROR"
      |akka.jvm-exit-on-fatal-error = off
      |akka.remote.transport-failure-detector.heartbeat-interval = $akkaHeartBeatIntervalSec s
      |akka.remote.transport-failure-detector.acceptable-heartbeat-pause = $akkaHeartBeatPausesSec s
      |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
      |akka.remote.netty.tcp.transport-class = "akka.remote.transport.netty.NettyTransport"
      |akka.remote.netty.tcp.hostname = "$host"
      |akka.remote.netty.tcp.port = $port
      |akka.remote.netty.tcp.tcp-nodelay = on
      |akka.remote.netty.tcp.connection-timeout = $akkaConnectTimeoutSec s
      |akka.remote.netty.tcp.maximum-frame-size = ${akkaFrameSizeBytes}B
      |akka.remote.netty.tcp.execution-pool-size = $akkaNumThreads
      |akka.actor.default-dispatcher.throughput = $akkaDispatcherThroughput
      |akka.log-config-on-start = $akkaLogConfig
      |akka.remote.log-remote-lifecycle-events = $akkaLifecycleEvents
      |akka.log-dead-letters = $akkaLifecycleEvents
      |akka.log-dead-letters-during-shutdown = $akkaLifecycleEvents
      """.stripMargin)

    val actorSystem = ActorSystem(name, akkaConf)
    val provider = actorSystem.asInstanceOf[ExtendedActorSystem].provider
    val boundPort = provider.getDefaultAddress.port.get
    (actorSystem, boundPort)
  }

  /** Returns the configured max frame size for Akka messages in bytes. */
  def maxFrameSizeBytes(conf: PSConf): Int = {
    val maxFrameSizeMB = Int.MaxValue / 1024 / 1024
    val frameSize = conf.get(AKKA_FRAME_SIZE)
    if (frameSize > maxFrameSizeMB) {
      throw new IllegalArgumentException(
        s"ps.akka.frameSize should not be greater than ${maxFrameSizeMB}MB")
    }
    (frameSize * 1024 * 1024).toInt
  }

  /**
   * Send a message to the given actor and get its result within a default timeout, or
   * throw a MLWheelException if this fails even after the specified number of retries.
   */
  def askWithRetry[T](message: Any, actor: ActorRef, conf: PSConf): T = {
    if (actor == null) {
      throw new PSException(s"Error sending message [message = $message]" +
        " as actor is null ")
    }

    val maxRetries = conf.get(AKKA_ASK_MAX_RETRIES)
    val retryIntervalMs = conf.get(AKKA_ASK_RETRY_INTERVAL)
    val timeout = Duration.create(conf.get(AKKA_ASK_TIMEOUT), "seconds")

    var retryTimes = 0
    var lastException: Exception = null
    while (retryTimes < maxRetries) {
      retryTimes += 1
      try {
        val future = actor.ask(message)(timeout)
        val result = Await.result(future, timeout)
        if (result == null) {
          throw new PSException("Actor returned null")
        }
        return result.asInstanceOf[T]
      } catch {
        case ie: InterruptedException => throw ie
        case e: Exception =>
          lastException = e
          logWarning(s"Error sending message [message = $message] in $retryTimes attempts", e)
      }
      Thread.sleep(retryIntervalMs)
    }

    throw new PSException(
      s"Error sending message [message = $message]", lastException)
  }

  /**
   * Make a PSMaster actor ref
   */
  def makePSMasterRef(
      name: String,
      conf: PSConf,
      actorSystem: ActorSystem): ActorRef = {
    val actorSystemName = PSContext.psMasterActorSystemName
    val psMasterHost: String = conf.get(PS_MASTER_HOST)
    val psMasterPort: Int = conf.get(PS_MASTER_PORT)
    assert(1024 <= psMasterPort && psMasterPort < 65536, s"Wrong ps master port: $psMasterPort")

    val url = address(protocol(actorSystem),
      actorSystemName, psMasterHost, psMasterPort, name)
    val timeout = Duration.create(conf.get(AKKA_LOOKUP_TIMEOUT), "seconds")
    logInfo(s"Connecting to $name: $url")
    Await.result(actorSystem.actorSelection(url).resolveOne(timeout), timeout)
  }

  /**
   * Make a PSWorker actor ref
   */
  def makePSWorkerRef(
      name: String,
      conf: PSConf,
      host: String,
      port: Int,
      actorSystem: ActorSystem): ActorRef = {
    val actorSystemName = PSContext.psWorkerActorSystemName
    val url = address(protocol(actorSystem), actorSystemName, host, port, name)
    val timeout = Duration.create(conf.get(AKKA_LOOKUP_TIMEOUT), "seconds")
    logInfo(s"Connecting to $name: $url")
    Await.result(actorSystem.actorSelection(url).resolveOne(timeout), timeout)
  }

  def protocol(actorSystem: ActorSystem): String = {
    val akkaConf = actorSystem.settings.config
    val sslProp = "akka.remote.netty.tcp.enable-ssl"
    protocol(akkaConf.hasPath(sslProp) && akkaConf.getBoolean(sslProp))
  }

  def protocol(ssl: Boolean = false): String = {
    if (ssl) {
      "akka.ssl.tcp"
    } else {
      "akka.tcp"
    }
  }

  def address(
      protocol: String,
      systemName: String,
      host: String,
      port: Any,
      actorName: String): String = {
    s"$protocol://$systemName@$host:$port/user/$actorName"
  }

}
