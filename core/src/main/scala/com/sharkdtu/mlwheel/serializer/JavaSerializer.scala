package com.sharkdtu.mlwheel.serializer

import java.io._
import java.nio.ByteBuffer

import scala.reflect.ClassTag

import com.sharkdtu.mlwheel.conf._
import com.sharkdtu.mlwheel.conf.PSConf
import com.sharkdtu.mlwheel.util.{ByteBufferInputStream, Utils}

private[mlwheel] class JavaSerializationStream(
    out: OutputStream,
    resetCnt: Int,
    extraDebugInfoEnabled: Boolean) extends SerializationStream {
  private val objOut = new ObjectOutputStream(out)
  private var count = 0

  /**
   * Calling reset to avoid memory leak:
   * http://stackoverflow.com/questions/1281549/memory-leak-traps-in-the-java-standard-api
   * But only call it every 100th time to avoid bloated serialization streams (when
   * the stream 'resets' object class descriptions have to be re-written)
   */
  def writeObject[T: ClassTag](t: T): SerializationStream = {
    try {
      objOut.writeObject(t)
    } catch {
      case e: NotSerializableException if extraDebugInfoEnabled =>
        throw SerializationDebugger.improveException(t, e)
    }
    count += 1
    if (resetCnt > 0 && count >= resetCnt) {
      objOut.reset()
      count = 0
    }
    this
  }

  def flush(): Unit = objOut.flush()
  def close(): Unit = objOut.close()
}

private[mlwheel] class JavaDeserializationStream(in: InputStream, loader: ClassLoader)
  extends DeserializationStream {
  private val objIn = new ObjectInputStream(in) {
    override def resolveClass(desc: ObjectStreamClass) =
      Class.forName(desc.getName, false, loader)
  }

  def readObject[T: ClassTag](): T = objIn.readObject().asInstanceOf[T]
  def close() { objIn.close() }
}


private[mlwheel] class JavaSerializerInstance(
    resetCnt: Int,
    extraDebugInfoEnabled: Boolean,
    defaultClassLoader: ClassLoader) extends SerializerInstance {

  override def serialize[T: ClassTag](t: T): ByteBuffer = {
    val bos = new ByteArrayOutputStream()
    val out = serializeStream(bos)
    out.writeObject(t)
    out.close()
    ByteBuffer.wrap(bos.toByteArray)
  }

  override def deserialize[T: ClassTag](bytes: ByteBuffer): T = {
    val bis = new ByteBufferInputStream(bytes)
    val in = deserializeStream(bis)
    in.readObject()
  }

  override def deserialize[T: ClassTag](bytes: ByteBuffer, loader: ClassLoader): T = {
    val bis = new ByteBufferInputStream(bytes)
    val in = deserializeStream(bis, loader)
    in.readObject()
  }

  override def serializeStream(s: OutputStream): SerializationStream = {
    new JavaSerializationStream(s, resetCnt, extraDebugInfoEnabled)
  }

  override def deserializeStream(s: InputStream): DeserializationStream = {
    new JavaDeserializationStream(s, defaultClassLoader)
  }

  def deserializeStream(s: InputStream, loader: ClassLoader): DeserializationStream = {
    new JavaDeserializationStream(s, loader)
  }
}

/**
 * A serializer that uses Java's built-in serialization.
 */
class JavaSerializer(conf: PSConf) extends Serializer with Externalizable {

  protected def this() = this(new PSConf())  // For deserialization only

  private var resetCnt = conf.get(SERIALIZER_OBJECT_STREAM_RESET_COUNT)
  private var extraDebugInfoEnabled = conf.get(SERIALIZER_EXTRA_DEBUG_INFO_ENABLED)

  override def newInstance(): SerializerInstance = {
    val classLoader = defaultClassLoader.getOrElse(Thread.currentThread.getContextClassLoader)
    new JavaSerializerInstance(resetCnt, extraDebugInfoEnabled, classLoader)
  }

  override def writeExternal(out: ObjectOutput): Unit = Utils.tryOrIOException {
    out.writeInt(resetCnt)
    out.writeBoolean(extraDebugInfoEnabled)
  }

  override def readExternal(in: ObjectInput): Unit = Utils.tryOrIOException {
    resetCnt = in.readInt()
    extraDebugInfoEnabled = in.readBoolean()
  }
}

