package com.sharkdtu.mlwheel.conf

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._

import com.sharkdtu.mlwheel.Logging
import com.sharkdtu.mlwheel.util.{ByteUtils, TimeUtils}

/**
 * Configuration used to set various parameters as key-value pairs.
 *
 * @param loadDefaults whether to load values from Java system properties
 */
class PSConf(loadDefaults: Boolean) extends Cloneable with Logging {

  /**
   * Create a [[PSConf]] object from system properties
   */
  def this() = this(true)

  private val settings = new ConcurrentHashMap[String, String]()

  private lazy val reader: ConfigReader = {
    new ConfigReader(new PSConfigProvider(settings))
  }

  if (loadDefaults) {
    loadFromSystemProperties()
  }

  private def loadFromSystemProperties(): PSConf = {
    // Load any "ps.*" system properties
    for ((key, value) <- sys.props if key.startsWith("ps.")) {
      set(key, value)
    }
    this
  }

  /**
   * Set a (key, value) parameter.
   *
   * @param key The parameter key
   * @param value The parameter value
   * @return The [[PSConf]] itself
   */
  def set(key: String, value: String): PSConf = {
    if (key == null) {
      throw new NullPointerException("null key")
    }
    if (value == null) {
      throw new NullPointerException("null value for " + key)
    }
    settings.put(key, value)
    this
  }

  /**
   * Set a pre-defined configuration entry.
   *
   * @note This is an internal API.
   */
  private[mlwheel] def set[T](entry: ConfigEntry[T], value: T): PSConf = {
    set(entry.key, entry.stringConverter(value))
    this
  }

  /**
   * Set a name for your ps.
   *
   * @param name The ps name
   * @return The [[PSConf]] itself
   */
  def setPSName(name: String): PSConf = {
    set("ps.name", name)
  }

  /**
   * Set multiple parameters together.
   *
   * @param settings The multiple parameters
   * @return The [[PSConf]] itself
   */
  def setAll(settings: Traversable[(String, String)]): PSConf = {
    settings.foreach { case (k, v) => set(k, v) }
    this
  }

  /**
   * Set a parameter if it isn't already configured.
   *
   * @param key The parameter key
   * @param value The parameter value
   * @return The [[PSConf]] itself
   */
  def setIfMissing(key: String, value: String): PSConf = {
    settings.putIfAbsent(key, value)
    this
  }

  /**
   * Set a ps master environment variable.
   *
   * @param variable The environment variable name
   * @param value The environment variable value
   * @return The [[PSConf]] itself
   */
  def setPSMasterEnv(variable: String, value: String): PSConf = {
    set("ps.master.env." + variable, value)
  }

  /**
   * Set a ps worker environment variable.
   *
   * @param variable The environment variable name
   * @param value The environment variable value
   * @return The [[PSConf]] itself
   */
  def setPSWorkerEnv(variable: String, value: String): PSConf = {
    set("ps.worker.env." + variable, value)
  }

  /**
   * Remove a parameter from the configuration.
   *
   * @param key The key of the parameter to be removed
   * @return The [[PSConf]] itself
   */
  def remove(key: String): PSConf = {
    settings.remove(key)
    this
  }

  /**
   * Remove a pre-defined entry from the configuration.
   *
   * @param entry The pre-defined entry to be removed
   * @return The [[PSConf]] itself
   * @note This is an internal API.
   */
  private[mlwheel] def remove(entry: ConfigEntry[_]): PSConf = {
    remove(entry.key)
  }

  /**
   * Get a parameter value from key.
   *
   * @param key The parameter key
   * @return The parameter value string
   * @throws NoSuchElementException if it's not set
   */
  def get(key: String): String = {
    getOption(key).getOrElse(throw new NoSuchElementException(key))
  }

  /**
   * Get a parameter value from key, falling back to a default if not set
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value string
   */
  def get(key: String, defaultValue: String): String = {
    getOption(key).getOrElse(defaultValue)
  }

  /**
   * Retrieves the value of a pre-defined configuration entry.
   *
   * - This is an internal API.
   * - The return type if defined by the configuration entry.
   * - This will throw an exception is the config is not optional and the value is not set.
   */
  private[mlwheel] def get[T](entry: ConfigEntry[T]): T = {
    entry.readFrom(reader)
  }

  /**
   * Get a parameter value as an Option from key
   *
   * @param key The parameter key
   * @return The parameter option value string
   */
  def getOption(key: String): Option[String] = {
    Option(settings.get(key))
  }

  /**
   * Get all parameters as a list of pairs.
   *
   * @return The list parameters
   */
  def getAll: Array[(String, String)] = {
    settings.entrySet().asScala.map(x => (x.getKey, x.getValue)).toArray
  }

  /**
   * Get all parameters that start with `prefix`.
   *
   * @param prefix The parameter key prefix
   * @return The list parameters
   */
  def getAllWithPrefix(prefix: String): Array[(String, String)] = {
    getAll.filter {
      case (k, v) => k.startsWith(prefix)
    }.map {
      case (k, v) => (k.substring(prefix.length), v)
    }
  }

  /**
   * Get a parameter value as an integer, falling back to a default if not set
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getInt(key: String, defaultValue: Int): Int = {
    getOption(key).map(_.toInt).getOrElse(defaultValue)
  }

  /**
   * Get a parameter value as a long, falling back to a default if not set
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getLong(key: String, defaultValue: Long): Long = {
    getOption(key).map(_.toLong).getOrElse(defaultValue)
  }

  /**
   * Get a parameter value as a double, falling back to a default if not set
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getDouble(key: String, defaultValue: Double): Double = {
    getOption(key).map(_.toDouble).getOrElse(defaultValue)
  }

  /**
   * Get a parameter value as a boolean, falling back to a default if not set
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getBoolean(key: String, defaultValue: Boolean): Boolean = {
    getOption(key).map(_.toBoolean).getOrElse(defaultValue)
  }

  /**
   * Get a time parameter value as seconds.
   * If no suffix is provided then seconds are assumed.
   *
   * @param key The parameter key
   * @return The parameter value
   * @throws NoSuchElementException if it's not set
   */
  def getTimeAsSec(key: String): Long = {
    TimeUtils.timeStringAsSec(get(key))
  }

  /**
   * Get a time parameter value as seconds, falling back to a default if not set.
   * If no suffix is provided then seconds are assumed.
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getTimeAsSec(key: String, defaultValue: String): Long = {
    TimeUtils.timeStringAsSec(get(key, defaultValue))
  }

  /**
   * Get a time parameter value as milliseconds.
   * If no suffix is provided then milliseconds are assumed.
   *
   * @param key The parameter key
   * @return The parameter value
   * @throws NoSuchElementException if it's not set
   */
  def getTimeAsMs(key: String): Long = {
    TimeUtils.timeStringAsMs(get(key))
  }

  /**
   * Get a time parameter value as milliseconds, falling back to a default if not set.
   * If no suffix is provided then milliseconds are assumed.
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getTimeAsMs(key: String, defaultValue: String): Long = {
    TimeUtils.timeStringAsMs(get(key, defaultValue))
  }

  /**
   * Get a time parameter value as Bytes.
   * If no suffix is provided then Bytes are assumed.
   *
   * @param key The parameter key
   * @return The parameter value
   * @throws NoSuchElementException if it's not set
   */
  def getSizeAsByte(key: String): Long = {
    ByteUtils.byteStringAsByte(get(key))
  }

  /**
   * Get a time parameter value as Bytes, falling back to a default if not set.
   * If no suffix is provided then Bytes are assumed.
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getSizeAsByte(key: String, defaultValue: String): Long = {
    ByteUtils.byteStringAsByte(get(key, defaultValue))
  }

  /**
   * Get a time parameter value as KB.
   * If no suffix is provided then KB are assumed.
   *
   * @param key The parameter key
   * @return The parameter value
   * @throws NoSuchElementException if it's not set
   */
  def getSizeAsKB(key: String): Long = {
    ByteUtils.byteStringAsKB(get(key))
  }

  /**
   * Get a time parameter value as KB, falling back to a default if not set.
   * If no suffix is provided then KB are assumed.
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getSizeAsKB(key: String, defaultValue: String): Long = {
    ByteUtils.byteStringAsKB(get(key, defaultValue))
  }

  /**
   * Get a time parameter value as MB.
   * If no suffix is provided then MB are assumed.
   *
   * @param key The parameter key
   * @return The parameter value
   * @throws NoSuchElementException if it's not set
   */
  def getSizeAsMB(key: String): Long = {
    ByteUtils.byteStringAsMB(get(key))
  }

  /**
   * Get a time parameter value as MB, falling back to a default if not set.
   * If no suffix is provided then MB are assumed.
   *
   * @param key The parameter key
   * @param defaultValue The default value
   * @return The parameter value
   */
  def getSizeAsMB(key: String, defaultValue: String): Long = {
    ByteUtils.byteStringAsMB(get(key, defaultValue))
  }

  /**
   * Get all ps master environment variables.
   *
   * @return The list environment variables of ps master
   */
  def getPSMasterEnvs: Seq[(String, String)] = {
    getAllWithPrefix("ps.master.env.")
  }

  /**
   * Get all ps worker environment variables.
   *
   * @return The list environment variables of ps worker
   */
  def getPSWorkerEnvs: Seq[(String, String)] = {
    getAllWithPrefix("ps.worker.env.")
  }

  /**
   * Does the configuration contains a given parameter?
   */
  def contains(key: String): Boolean = {
    settings.containsKey(key)
  }

  /**
   * Does the configuration contains a given entry?
   */
  private[mlwheel] def contains(entry: ConfigEntry[_]): Boolean = contains(entry.key)

  /**
   * Deep copy this object
   *
   * @return A new [[PSConf]] object
   */
  override def clone: PSConf = {
    val cloned = new PSConf(false)
    settings.entrySet().asScala.foreach { e =>
      cloned.set(e.getKey, e.getValue)
    }
    cloned
  }

  /**
   * Return a string listing all keys and values, one per line.
   * This is useful to print the configuration out for debugging.
   *
   * @return The debug string
   */
  def toDebugString: String = {
    getAll.sorted.map { case (k, v) => k + "=" + v }.mkString("\n")
  }

}
