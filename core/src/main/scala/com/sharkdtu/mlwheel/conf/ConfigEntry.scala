package com.sharkdtu.mlwheel.conf

/**
 * An entry contains all meta information for a configuration.
 *
 * When applying variable substitution to config values, only references starting with "ps."
 * are considered in the default namespace. For known PS configuration keys (i.e. those
 * created using `ConfigBuilder`), references will also consider the default value when it exists.
 *
 * Variable expansion is also applied to the default values of config entries that have a default
 * value declared as a string.
 *
 * @param key the key for the configuration
 * @param valueConverter how to convert a string to the value. It should throw an exception if the
 *                       string does not have the required format.
 * @param stringConverter how to convert a value to a string that the user can use it as a valid
 *                        string value. It's usually `toString`. But sometimes, a custom converter
 *                        is necessary. E.g., if T is List[String], `a, b, c` is better than
 *                        `List(a, b, c)`.
 * @param doc the documentation for the configuration
 * @param isPublic if this configuration is public to the user. If it's `false`, this
 *                 configuration is only used internally and we should not expose it to users.
 * @tparam T the value type
 */
private[mlwheel] abstract class ConfigEntry[T] (
    val key: String,
    val valueConverter: String => T,
    val stringConverter: T => String,
    val doc: String,
    val isPublic: Boolean) {

  import ConfigEntry._

  registerEntry(this)

  def defaultValueString: String

  def readFrom(reader: ConfigReader): T

  def defaultValue: Option[T] = None

  override def toString: String = {
    s"ConfigEntry(key=$key, defaultValue=$defaultValueString, doc=$doc, public=$isPublic)"
  }

}

private[mlwheel] class ConfigEntryWithDefault[T] (
    key: String,
    _defaultValue: T,
    valueConverter: String => T,
    stringConverter: T => String,
    doc: String,
    isPublic: Boolean)
    extends ConfigEntry(key, valueConverter, stringConverter, doc, isPublic) {

  override def defaultValue: Option[T] = Some(_defaultValue)

  override def defaultValueString: String = stringConverter(_defaultValue)

  def readFrom(reader: ConfigReader): T = {
    reader.get(key).map(valueConverter).getOrElse(_defaultValue)
  }

}

private[mlwheel] class ConfigEntryWithDefaultString[T] (
    key: String,
    _defaultValue: String,
    valueConverter: String => T,
    stringConverter: T => String,
    doc: String,
    isPublic: Boolean)
    extends ConfigEntry(key, valueConverter, stringConverter, doc, isPublic) {

  override def defaultValue: Option[T] = Some(valueConverter(_defaultValue))

  override def defaultValueString: String = _defaultValue

  def readFrom(reader: ConfigReader): T = {
    val value = reader.get(key).getOrElse(reader.substitute(_defaultValue))
    valueConverter(value)
  }

}

/**
 * A config entry whose default value is defined by another config entry.
 */
private[mlwheel] class FallbackConfigEntry[T] (
    key: String,
    doc: String,
    isPublic: Boolean,
    private[conf] val fallback: ConfigEntry[T])
    extends ConfigEntry[T](key, fallback.valueConverter, fallback.stringConverter, doc, isPublic) {

  override def defaultValueString: String = s"<value of ${fallback.key}>"

  override def readFrom(reader: ConfigReader): T = {
    reader.get(key).map(valueConverter).getOrElse(fallback.readFrom(reader))
  }

}

private[mlwheel] object ConfigEntry {

  private val knownConfigs = new java.util.concurrent.ConcurrentHashMap[String, ConfigEntry[_]]()

  def registerEntry(entry: ConfigEntry[_]): Unit = {
    val existing = knownConfigs.putIfAbsent(entry.key, entry)
    require(existing == null, s"Config entry ${entry.key} already registered!")
  }

  def findEntry(key: String): ConfigEntry[_] = knownConfigs.get(key)

}
