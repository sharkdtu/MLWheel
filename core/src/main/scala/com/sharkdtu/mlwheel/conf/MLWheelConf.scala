package com.sharkdtu.mlwheel.conf

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.util.{ByteUtils, TimeUtils}

/**
 * Configuration for MLWheel. Used to set various parameters as key-value pairs.
 *
 * @param loadDefaults whether to load values from Java system properties
 */
class MLWheelConf(loadDefaults: Boolean) extends Cloneable with Logging {

  def this() = this(true)

  private val settings = new ConcurrentHashMap[String, String]()

  private lazy val reader: ConfigReader = {
    new ConfigReader(new MLWheelConfigProvider(settings))
  }

  if (loadDefaults) {
    loadFromSystemProperties()
  }

  private def loadFromSystemProperties(): MLWheelConf = {
    // Load any mlwheel.* system properties
    for ((key, value) <- sys.props if key.startsWith("mlwheel.")) {
      set(key, value)
    }
    this
  }

  def set(key: String, value: String): MLWheelConf = {
    if (key == null) {
      throw new NullPointerException("null key")
    }
    if (value == null) {
      throw new NullPointerException("null value for " + key)
    }
    settings.put(key, value)
    this
  }

  private[mlwheel] def set[T](entry: ConfigEntry[T], value: T): MLWheelConf = {
    set(entry.key, entry.stringConverter(value))
    this
  }

  /** Set a name for your application. */
  def setAppName(name: String): MLWheelConf = {
    set("mlwheel.app.name", name)
  }

  /** Set multiple parameters together */
  def setAll(settings: Traversable[(String, String)]): MLWheelConf = {
    settings.foreach { case (k, v) => set(k, v) }
    this
  }

  /** Set a parameter if it isn't already configured */
  def setIfMissing(key: String, value: String): MLWheelConf = {
    settings.putIfAbsent(key, value)
    this
  }

  def setMasterEnv(variable: String, value: String): MLWheelConf = {
    set("mlwheel.masterEnv." + variable, value)
  }

  def setPSEnv(variable: String, value: String): MLWheelConf = {
    set("mlwheel.psEnv." + variable, value)
  }

  /** Remove a parameter from the configuration */
  def remove(key: String): MLWheelConf = {
    settings.remove(key)
    this
  }

  private[mlwheel] def remove(entry: ConfigEntry[_]): MLWheelConf = {
    remove(entry.key)
  }

  /** Get a parameter; throws a NoSuchElementException if it's not set */
  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }

  /** Get a parameter, falling back to a default if not set */
  def get(key: String, defaultValue: String): String = {
    getOption(key).getOrElse(defaultValue)
  }

  /**
   * Retrieves the value of a pre-defined configuration entry.
   *
   * - This is an internal MLWheel API.
   * - The return type if defined by the configuration entry.
   * - This will throw an exception is the config is not optional and the value is not set.
   */
  private[mlwheel] def get[T](entry: ConfigEntry[T]): T = {
    entry.readFrom(reader)
  }

  /** Get a parameter as an Option */
  def getOption(key: String): Option[String] = {
    Option(settings.get(key))
  }

  /** Get all parameters as a list of pairs */
  def getAll: Array[(String, String)] = {
    settings.entrySet().asScala.map(x => (x.getKey, x.getValue)).toArray
  }

  /**
   * Get all parameters that start with `prefix`
   */
  def getAllWithPrefix(prefix: String): Array[(String, String)] = {
    getAll.filter { case (k, v) => k.startsWith(prefix) }
      .map { case (k, v) => (k.substring(prefix.length), v) }
  }

  /** Get a parameter as an integer, falling back to a default if not set */
  def getInt(key: String, defaultValue: Int): Int = {
    getOption(key).map(_.toInt).getOrElse(defaultValue)
  }

  /** Get a parameter as a long, falling back to a default if not set */
  def getLong(key: String, defaultValue: Long): Long = {
    getOption(key).map(_.toLong).getOrElse(defaultValue)
  }

  /** Get a parameter as a double, falling back to a default if not set */
  def getDouble(key: String, defaultValue: Double): Double = {
    getOption(key).map(_.toDouble).getOrElse(defaultValue)
  }

  /** Get a parameter as a boolean, falling back to a default if not set */
  def getBoolean(key: String, defaultValue: Boolean): Boolean = {
    getOption(key).map(_.toBoolean).getOrElse(defaultValue)
  }

  /**
   * Get a time parameter as seconds; throws a NoSuchElementException if it's not set.
   * If no suffix is provided then seconds are assumed.
   */
  def getTimeAsSec(key: String): Long = {
    TimeUtils.timeStringAsSec(get(key))
  }

  /**
   * Get a time parameter as seconds, falling back to a default if not set.
   * If no suffix is provided then seconds are assumed.
   */
  def getTimeAsSec(key: String, defaultValue: String): Long = {
    TimeUtils.timeStringAsSec(get(key, defaultValue))
  }

  /**
   * Get a time parameter as milliseconds; throws a NoSuchElementException if it's not set.
   * If no suffix is provided then milliseconds are assumed.
   */
  def getTimeAsMs(key: String): Long = {
    TimeUtils.timeStringAsMs(get(key))
  }

  /**
   * Get a time parameter as milliseconds, falling back to a default if not set.
   * If no suffix is provided then milliseconds are assumed.
   */
  def getTimeAsMs(key: String, defaultValue: String): Long = {
    TimeUtils.timeStringAsMs(get(key, defaultValue))
  }

  /**
   * Get a size parameter as bytes; throws a NoSuchElementException if it's not set.
   * If no suffix is provided then bytes are assumed.
   */
  def getSizeAsByte(key: String): Long = {
    ByteUtils.byteStringAsByte(get(key))
  }

  /**
   * Get a size parameter as bytes, falling back to a default if not set.
   * If no suffix is provided then bytes are assumed.
   */
  def getSizeAsByte(key: String, defaultValue: String): Long = {
    ByteUtils.byteStringAsByte(get(key, defaultValue))
  }

  /**
   * Get a size parameter as Kibibytes; throws a NoSuchElementException if it's not set.
   * If no suffix is provided then Kibibytes are assumed.
   */
  def getSizeAsKB(key: String): Long = {
    ByteUtils.byteStringAsKB(get(key))
  }

  /**
   * Get a size parameter as Kibibytes, falling back to a default if not set.
   * If no suffix is provided then Kibibytes are assumed.
   */
  def getSizeAsKB(key: String, defaultValue: String): Long = {
    ByteUtils.byteStringAsKB(get(key, defaultValue))
  }

  /**
   * Get a size parameter as Mebibytes; throws a NoSuchElementException if it's not set.
   * If no suffix is provided then Mebibytes are assumed.
   */
  def getSizeAsMB(key: String): Long = {
    ByteUtils.byteStringAsMB(get(key))
  }

  /**
   * Get a size parameter as Mebibytes, falling back to a default if not set.
   * If no suffix is provided then Mebibytes are assumed.
   */
  def getSizeAsMB(key: String, defaultValue: String): Long = {
    ByteUtils.byteStringAsMB(get(key, defaultValue))
  }

  /** Get all master environment variables set on this MLWheel */
  def getMaterEnvs: Seq[(String, String)] = {
    getAllWithPrefix("mlwheel.masterEnv.")
  }

  /** Get all ps environment variables set on this MLWheel */
  def getPsEnvs: Seq[(String, String)] = {
    getAllWithPrefix("mlwheel.psEnv.")
  }

  /** Does the configuration contain a given parameter? */
  def contains(key: String): Boolean = {
    settings.containsKey(key)
  }

  private[mlwheel] def contains(entry: ConfigEntry[_]): Boolean = contains(entry.key)

  /** Copy this object */
  override def clone: MLWheelConf = {
    val cloned = new MLWheelConf(false)
    settings.entrySet().asScala.foreach { e =>
      cloned.set(e.getKey, e.getValue)
    }
    cloned
  }

  /**
   * Return a string listing all keys and values, one per line. This is useful to print the
   * configuration out for debugging.
   */
  def toDebugString: String = {
    getAll.sorted.map { case (k, v) => k + "=" + v }.mkString("\n")
  }

}

private[mlwheel] object MLWheelConf extends Logging {


}
