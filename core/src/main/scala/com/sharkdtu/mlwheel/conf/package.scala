package com.sharkdtu.mlwheel

import java.util.concurrent.TimeUnit

import com.sharkdtu.mlwheel.util.ByteUnit
import com.sharkdtu.mlwheel.serializer.JavaSerializer

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

  private[mlwheel] val SERIALIZER_OBJECT_STREAM_RESET_COUNT =
    ConfigBuilder("ps.serializer.objectStreamReset.count")
      .intConf
      .createWithDefault(100)

  private[mlwheel] val SERIALIZER_EXTRA_DEBUG_INFO_ENABLED =
    ConfigBuilder("ps.serializer.extraDebugInfo.enabled")
      .booleanConf
      .createWithDefault(true)

  private[mlwheel] val FILE_TRANSFER_TO_ENABLED =
    ConfigBuilder("ps.file.transferTo.enabled")
      .booleanConf
      .createWithDefault(true)

  private[mlwheel] val PS_SERIALIZER =
    ConfigBuilder("ps.serializer")
      .stringConf
      .createWithDefault("java")

  private[mlwheel] val PS_IO_COMPRESSION_CODEC =
    ConfigBuilder("ps.io.compression.codec")
      .stringConf
      .createWithDefault("lz4")

  private[mlwheel] val PS_IO_COMPRESSION_BLOCKSIZE =
    ConfigBuilder("ps.io.compression.blockSize")
      .byteConf(ByteUnit.BYTE)
      .createWithDefaultString("32KB")

  // ============================================ //
  //                 Master settings              //
  // ============================================ //
  private[mlwheel] val PS_MASTER_CLIENT_CLEANER_INTERVAL =
    ConfigBuilder("ps.master.client.cleaner.interval")
      .timeConf(TimeUnit.MILLISECONDS)
      .createWithDefaultString("3000ms")

  private[mlwheel] val PS_MASTER_CLIENT_CAPACITY =
    ConfigBuilder("ps.master.client.capacity")
      .intConf
      .createWithDefault(100)

  private[mlwheel] val PS_MASTER_WORKER_CAPACITY =
    ConfigBuilder("ps.master.worker.capacity")
      .intConf
      .createWithDefault(100)

  private[mlwheel] val PS_MASTER_MESSAGE_PROCESSOR_POOL_SIZE =
    ConfigBuilder("ps.master.message.processor.pool.size")
      .intConf
      .createWithDefault(2 * Runtime.getRuntime.availableProcessors())

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

  // ============================================ //
  //             PSVariable settings              //
  // ============================================ //
  private[mlwheel] val PARTITION_MIN_ELEMENTS =
    ConfigBuilder("ps.partition.minElements")
      .doc("minimum number of elements in one partition.")
      .intConf
      .createWithDefault(1000)

  private[mlwheel] val PARTITION_MAX_ELEMENTS =
    ConfigBuilder("ps.partition.maxElements")
      .doc("maximum number of elements in one partition.")
      .intConf
      .createWithDefault(10000)
}
