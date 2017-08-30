package com.sharkdtu.mlwheel.conf

import java.util.concurrent.TimeUnit

import com.sharkdtu.mlwheel.util.{ByteUnit, ByteUtils, TimeUtils}

private object ConfigHelpers {

  def toNumber[T](s: String, converter: String => T, key: String, configType: String): T = {
    try {
      converter(s)
    } catch {
      case _: NumberFormatException =>
        throw new IllegalArgumentException(s"$key should be $configType, but was $s")
    }
  }

  def toBoolean(s: String, key: String): Boolean = {
    try {
      s.toBoolean
    } catch {
      case _: IllegalArgumentException =>
        throw new IllegalArgumentException(s"$key should be boolean, but was $s")
    }
  }

  def stringToSeq[T](str: String, converter: String => T): Seq[T] = {
    str.split(",").map(_.trim()).filter(_.nonEmpty).map(converter)
  }

  def seqToString[T](v: Seq[T], stringConverter: T => String): String = {
    v.map(stringConverter).mkString(",")
  }

  def timeFromString(str: String, unit: TimeUnit): Long = TimeUtils.timeStringAs(str, unit)

  def timeToString(v: Long, unit: TimeUnit): String = TimeUnit.MILLISECONDS.convert(v, unit) + "ms"

  def byteFromString(str: String, unit: ByteUnit): Long = {
    val (input, multiplier) =
      if (str.length() > 0 && str.charAt(0) == '-') {
        (str.substring(1), -1)
      } else {
        (str, 1)
      }
    multiplier * ByteUtils.byteStringAs(input, unit)
  }

  def byteToString(v: Long, unit: ByteUnit): String = unit.convertTo(v, ByteUnit.BYTE) + "Byte"

}

/**
 * A type-safe config builder. Provides methods for transforming the input data (which can be
 * used, e.g., for validation) and creating the final config entry.
 *
 * One of the methods that return a [[ConfigEntry]] must be called to create a config entry that
 * can be used with [[MLWheelConf]].
 */
private[mlwheel] class TypedConfigBuilder[T](
  val parent: ConfigBuilder,
  val converter: String => T,
  val stringConverter: T => String) {

  import ConfigHelpers._

  def this(parent: ConfigBuilder, converter: String => T) = {
    this(parent, converter, Option(_).map(_.toString).orNull)
  }

  /** Apply a transformation to the user-provided values of the config entry. */
  def transform(fn: T => T): TypedConfigBuilder[T] = {
    new TypedConfigBuilder(parent, s => fn(converter(s)), stringConverter)
  }

  /** Check that user-provided values for the config match a pre-defined set. */
  def checkValues(validValues: Set[T]): TypedConfigBuilder[T] = {
    transform { v =>
      if (!validValues.contains(v)) {
        throw new IllegalArgumentException(
          s"The value of ${parent.key} should be one of ${validValues.mkString(", ")}, but was $v")
      }
      v
    }
  }

  /** Turns the config entry into a sequence of values of the underlying type. */
  def toSequence: TypedConfigBuilder[Seq[T]] = {
    new TypedConfigBuilder[Seq[T]](
      parent, stringToSeq(_, converter), seqToString(_, stringConverter))
  }

  /** Creates a [[ConfigEntry]] that does not have a default value. */
  def createOptional: OptionalConfigEntry[T] = {
    val entry = new OptionalConfigEntry[T](
      parent.key, converter, stringConverter, parent._doc, parent._public)
    parent._onCreate.foreach(_(entry))
    entry
  }

  /** Creates a [[ConfigEntry]] that has a default value. */
  def createWithDefault(default: T): ConfigEntry[T] = {
    // Treat "String" as a special case, so that both createWithDefault and createWithDefaultString
    // behave the same w.r.t. variable expansion of default values.
    if (default.isInstanceOf[String]) {
      createWithDefaultString(default.asInstanceOf[String])
    } else {
      val transformedDefault = converter(stringConverter(default))
      val entry = new ConfigEntryWithDefault[T](parent.key, transformedDefault, converter,
        stringConverter, parent._doc, parent._public)
      parent._onCreate.foreach(_(entry))
      entry
    }
  }

  /**
   * Creates a [[ConfigEntry]] that has a default value. The default value is provided as a
   * [[String]] and must be a valid value for the entry.
   */
  def createWithDefaultString(default: String): ConfigEntry[T] = {
    val entry = new ConfigEntryWithDefaultString[T](
      parent.key, default, converter, stringConverter, parent._doc, parent._public)
    parent._onCreate.foreach(_(entry))
    entry
  }

}

/**
 * Basic builder for MLWheel configurations. Provides methods for creating type-specific builders.
 *
 * @see TypedConfigBuilder
 */
private[mlwheel] case class ConfigBuilder(key: String) {

  import ConfigHelpers._

  private[conf] var _public = true
  private[conf] var _doc = ""
  private[conf] var _onCreate: Option[ConfigEntry[_] => Unit] = None

  def internal(): ConfigBuilder = {
    _public = false
    this
  }

  def doc(s: String): ConfigBuilder = {
    _doc = s
    this
  }

  def onCreate(callback: ConfigEntry[_] => Unit): ConfigBuilder = {
    _onCreate = Option(callback)
    this
  }

  def intConf: TypedConfigBuilder[Int] = {
    new TypedConfigBuilder(this, toNumber(_, _.toInt, key, "int"))
  }

  def longConf: TypedConfigBuilder[Long] = {
    new TypedConfigBuilder(this, toNumber(_, _.toLong, key, "long"))
  }

  def doubleConf: TypedConfigBuilder[Double] = {
    new TypedConfigBuilder(this, toNumber(_, _.toDouble, key, "double"))
  }

  def booleanConf: TypedConfigBuilder[Boolean] = {
    new TypedConfigBuilder(this, toBoolean(_, key))
  }

  def stringConf: TypedConfigBuilder[String] = {
    new TypedConfigBuilder(this, v => v)
  }

  def timeConf(unit: TimeUnit): TypedConfigBuilder[Long] = {
    new TypedConfigBuilder(this, timeFromString(_, unit), timeToString(_, unit))
  }

  def fallbackConf[T](fallback: ConfigEntry[T]): ConfigEntry[T] = {
    new FallbackConfigEntry(key, _doc, _public, fallback)
  }

}
