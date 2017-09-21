package com.sharkdtu.mlwheel.serializer

import java.io._
import java.nio.ByteBuffer

import scala.reflect.ClassTag

import com.sharkdtu.mlwheel.conf.PSConf
import com.sharkdtu.mlwheel.util.NextIterator

/**
 * A serializer. Because some serialization libraries are not thread safe, this class is used to
 * create [[SerializerInstance]] objects that do the actual
 * serialization and are guaranteed to only be called from one thread at a time.
 *
 * Implementations of this abstract class should implement:
 *
 * 1. a zero-arg constructor or a constructor that accepts a [[PSConf]]
 * as parameter. If both constructors are defined, the latter takes precedence.
 *
 * 2. Java serialization interface.
 *
 */
abstract class Serializer {

  /**
   * Default ClassLoader to use in deserialization.
   * Implementations of [[Serializer]] should make sure it is using this when set.
   */
  @volatile protected var defaultClassLoader: Option[ClassLoader] = None

  /**
   * Sets a class loader for the serializer to use in deserialization.
   *
   * @return this Serializer object
   */
  def setDefaultClassLoader(classLoader: ClassLoader): Serializer = {
    defaultClassLoader = Some(classLoader)
    this
  }

  /** Creates a new [[SerializerInstance]]. */
  def newInstance(): SerializerInstance
}

/**
 * An instance of a serializer, for use by one thread at a time.
 */
abstract class SerializerInstance {
  def serialize[T: ClassTag](t: T): ByteBuffer

  def deserialize[T: ClassTag](bytes: ByteBuffer): T

  def deserialize[T: ClassTag](bytes: ByteBuffer, loader: ClassLoader): T

  def serializeStream(s: OutputStream): SerializationStream

  def deserializeStream(s: InputStream): DeserializationStream
}

/**
 * A stream for writing serialized objects.
 */
abstract class SerializationStream {
  def writeObject[T: ClassTag](t: T): SerializationStream
  def flush(): Unit
  def close(): Unit

  def writeAll[T: ClassTag](iter: Iterator[T]): SerializationStream = {
    while (iter.hasNext) {
      writeObject(iter.next())
    }
    this
  }
}


/**
 * A stream for reading serialized objects.
 */
abstract class DeserializationStream {
  def readObject[T: ClassTag](): T
  def close(): Unit

  /**
   * Read the elements of this stream through an iterator. This can only be called once, as
   * reading each element will consume data from the input source.
   */
  def asIterator: Iterator[Any] = new NextIterator[Any] {
    override protected def getNext() = {
      try {
        readObject[Any]()
      } catch {
        case eof: EOFException =>
          finished = true
      }
    }

    override protected def close() {
      DeserializationStream.this.close()
    }
  }
}