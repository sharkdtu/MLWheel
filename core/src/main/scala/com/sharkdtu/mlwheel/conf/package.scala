package com.sharkdtu.mlwheel

import java.util.concurrent.TimeUnit

import com.sharkdtu.mlwheel.util.ByteUnit

/**
 * Some pre-defined conf entries for internal use
 */
package object conf {

  // ============================================ //
  //                General settings              //
  // ============================================ //
  private[mlwheel] val PS_CLIENT_PORT =
    ConfigBuilder("ps.client.port")
      .doc("Specify the client port, used in ps client.")
      .intConf
      .createWithDefault(0)

  private[mlwheel] val PS_MASTER_HOST =
    ConfigBuilder("ps.master.host")
      .doc("Specify the master host, used in ps client, master, worker.")
      .stringConf
      .createWithDefault("localhost")

  private[mlwheel] val PS_MASTER_PORT =
    ConfigBuilder("ps.master.port")
      .doc("Specify the master port, used in ps master.")
      .intConf
      .createWithDefault(0)

  private[mlwheel] val PS_WORKER_PORT =
    ConfigBuilder("ps.worker.port")
      .doc("Specify the worker port, used in ps worker.")
      .intConf
      .createWithDefault(0)

  private[mlwheel] val PS_PORT_MAX_RETRIES =
    ConfigBuilder("ps.port.maxRetries")
      .intConf
      .createWithDefault(16)

  // ============================================ //
  //                  Akka settings               //
  // ============================================ //
  private[mlwheel] val AKKA_NUM_THREADS =
    ConfigBuilder("ps.akka.numThreads")
      .intConf
      .createWithDefault(4)

  private[mlwheel] val AKKA_DISPATCHER_THROUGHPUT =
    ConfigBuilder("ps.akka.dispatcher.throughput")
      .intConf
      .createWithDefault(16)

  private[mlwheel] val AKKA_CONNECT_TIMEOUT =
    ConfigBuilder("ps.akka.connect.timeout")
      .timeConf(TimeUnit.SECONDS)
      .createWithDefaultString("120s")

  private[mlwheel] val AKKA_FRAME_SIZE =
    ConfigBuilder("ps.akka.frameSize")
      .byteConf(ByteUnit.MB)
      .createWithDefaultString("20MB")

  private[mlwheel] val AKKA_LOG_LIFT_CYCLE_EVENTS =
    ConfigBuilder("ps.akka.logLifecycleEvents")
      .booleanConf
      .createWithDefault(false)

  private[mlwheel] val AKKA_LOG_CONFIG =
    ConfigBuilder("ps.akka.logAkkaConfig")
      .booleanConf
      .createWithDefault(false)

  private[mlwheel] val AKKA_HEARTBEAT_PAUSES =
    ConfigBuilder("ps.akka.heartbeat.pauses")
      .timeConf(TimeUnit.SECONDS)
      .createWithDefaultString("6000s")

  private[mlwheel] val AKKA_HEARTBEAT_INTERVAL =
    ConfigBuilder("ps.akka.heartbeat.interval")
      .timeConf(TimeUnit.SECONDS)
      .createWithDefaultString("1000s")

  private[mlwheel] val AKKA_ASK_TIMEOUT =
    ConfigBuilder("ps.akka.ask.timeout")
      .timeConf(TimeUnit.SECONDS)
      .createWithDefaultString("30s")

  private[mlwheel] val AKKA_LOOKUP_TIMEOUT =
    ConfigBuilder("ps.akka.lookup.timeout")
      .timeConf(TimeUnit.SECONDS)
      .createWithDefaultString("30s")

  private[mlwheel] val AKKA_ASK_MAX_RETRIES =
    ConfigBuilder("ps.akka.ask.maxRetries")
      .intConf
      .createWithDefault(3)

  private[mlwheel] val AKKA_ASK_RETRY_INTERVAL =
    ConfigBuilder("ps.akka.ask.retry.interval")
      .timeConf(TimeUnit.MILLISECONDS)
      .createWithDefaultString("3000ms")
}
