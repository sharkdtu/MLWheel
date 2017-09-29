package com.sharkdtu.mlwheel.io

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, EOFException}

import com.sharkdtu.mlwheel.PSFunSuite
import com.sharkdtu.mlwheel.conf.{PSConf, _}

class CompressionCodecSuite extends PSFunSuite {
  val conf = new PSConf(false)

  def testCodec(codec: CompressionCodec) {
    // Write 1000 integers to the output stream, compressed.
    val outputStream = new ByteArrayOutputStream()
    val out = codec.compressedOutputStream(outputStream)
    for (i <- 1 until 1000) {
      out.write(i % 256)
    }
    out.close()

    // Read the 1000 integers back.
    val inputStream = new ByteArrayInputStream(outputStream.toByteArray)
    val in = codec.compressedInputStream(inputStream)
    for (i <- 1 until 1000) {
      assert(in.read() === i % 256)
    }
    in.close()
  }

  test("default compression codec") {
    val codec = CompressionCodec.create(conf)
    assert(codec.getClass === classOf[LZ4CompressionCodec])
    testCodec(codec)
  }

  test("compress array bytes") {
    val codec = CompressionCodec.create(conf)
    val arr = Array.ofDim[Double](1000)
    for (i <- 0 until 1000) arr(i) = 1.0 * i
    val buf = java.nio.ByteBuffer.allocate(8 * arr.length)
    buf.asDoubleBuffer.put(java.nio.DoubleBuffer.wrap(arr))

    val outputStream = new ByteArrayOutputStream()
    val out = codec.compressedOutputStream(outputStream)
    out.write(buf.array())
    out.close()

    val inputStream = new ByteArrayInputStream(outputStream.toByteArray)
    val in = codec.compressedInputStream(inputStream)
    val bytes = Array.ofDim[Byte](8 * 1000)
    var amountRead = 0
    while (amountRead < 8 * 1000) {
      val ret = in.read(bytes)
      if (ret == -1) {
        throw new EOFException("End of file before fully reading buffer")
      }
      amountRead += ret
    }
    in.close()
    assert(bytes.length === 8 * 1000)
  }

  test("bad compression codec") {
    intercept[IllegalArgumentException] {
      conf.set(PS_IO_COMPRESSION_CODEC, "foo")
      CompressionCodec.create(conf)
    }
  }
}
