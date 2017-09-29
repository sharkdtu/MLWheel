package com.sharkdtu.mlwheel.io

import java.io.{InputStream, OutputStream}

import net.jpountz.lz4.{LZ4BlockInputStream, LZ4BlockOutputStream}

import com.sharkdtu.mlwheel.conf.{PSConf, _}


/**
 * CompressionCodec allows the customization of choosing different compression implementations
 * to be used in block storage.
 */
trait CompressionCodec {
  def compressedOutputStream(s: OutputStream): OutputStream
  def compressedInputStream(s: InputStream): InputStream
}

private[mlwheel] object CompressionCodec {
  def create(conf: PSConf): CompressionCodec = {
    conf.get(PS_IO_COMPRESSION_CODEC) match {
      case "lz4" => new LZ4CompressionCodec(conf)
      case codec => throw new IllegalArgumentException(s"Unknown codec: $codec")
    }
  }
}

/**
 * LZ4 implementation of [[CompressionCodec]].
 * Block size can be configured by `ps.io.compression.blockSize`.
 */
class LZ4CompressionCodec(conf: PSConf) extends CompressionCodec {

  override def compressedOutputStream(s: OutputStream): OutputStream = {
    val blockSize = conf.get(PS_IO_COMPRESSION_BLOCKSIZE).toInt
    new LZ4BlockOutputStream(s, blockSize)
  }

  override def compressedInputStream(s: InputStream): InputStream = new LZ4BlockInputStream(s)
}