package com.sharkdtu.mlwheel.util

import java.util.concurrent.TimeUnit
import java.util.regex.{Matcher, Pattern}

object TimeUtils {

  private val timeSuffixes = Map(
    "us" -> TimeUnit.MICROSECONDS,
    "ms" -> TimeUnit.MILLISECONDS,
    "s" -> TimeUnit.SECONDS,
    "m" -> TimeUnit.MINUTES,
    "min" -> TimeUnit.MINUTES,
    "h" -> TimeUnit.HOURS,
    "d" -> TimeUnit.DAYS)

  /**
    * Convert a passed time string (e.g. 50s, 100ms, or 250us) to a time count in the given unit.
    * The unit is also considered the default if the given string does not specify a unit.
    */
  def timeStringAs(str: String, unit: TimeUnit): Long = {
    val lower = str.toLowerCase.trim
    try {
      val m = Pattern.compile("(-?[0-9]+)([a-z]+)?").matcher(lower)
      if (!m.matches) throw new NumberFormatException(s"Failed to parse time string: $str")
      val value = m.group(1).toLong
      val suffix = m.group(2)
      // Check for invalid suffixes
      if (suffix != null && !timeSuffixes.contains(suffix)) {
        throw new NumberFormatException(s"Invalid suffix: \"$suffix\"")
      }
      // If suffix is valid use that, otherwise none was provided and use the default passed
      unit.convert(value, if (suffix != null) timeSuffixes(suffix) else unit)
    } catch {
      case e: NumberFormatException =>
        val timeError = "Time must be specified as seconds (s), " +
          "milliseconds (ms), microseconds (us), minutes (m or min), hour (h), or day (d). " +
          "E.g. 50s, 100ms, or 250us."
        throw new NumberFormatException(timeError + "\n" + e.getMessage)
    }
  }

  /**
    * Convert a time parameter such as (50s, 100ms, or 250us) to milliseconds for internal use. If
    * no suffix is provided, the passed number is assumed to be in ms.
    */
  def timeStringAsMs(str: String): Long = timeStringAs(str, TimeUnit.MILLISECONDS)

  /**
    * Convert a time parameter such as (50s, 100ms, or 250us) to seconds for internal use. If
    * no suffix is provided, the passed number is assumed to be in seconds.
    */
  def timeStringAsSec(str: String): Long = timeStringAs(str, TimeUnit.SECONDS)

}
