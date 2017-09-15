package com.sharkdtu.mlwheel.util

import java.util.concurrent.TimeUnit

private[mlwheel] object TimeUtils {

  private val timeSuffixes = Map(
    "us" -> TimeUnit.MICROSECONDS,
    "ms" -> TimeUnit.MILLISECONDS,
    "s" -> TimeUnit.SECONDS,
    "min" -> TimeUnit.MINUTES,
    "h" -> TimeUnit.HOURS,
    "d" -> TimeUnit.DAYS)

  /**
   * Convert a passed time string (e.g. 50s, 100ms, or 250us) to a time count in the given unit.
   * The unit is also considered the default if the given string does not specify a unit.
   */
  def timeStringAs(str: String, unit: TimeUnit): Long = {
    val lower = str.toLowerCase.trim
    val pattern = "([0-9]+)([a-z]+)?".r
    lower match {
      case pattern(d, u) =>
        val value = d.toLong
        if (u != null && !timeSuffixes.contains(u)) {
          throw new NumberFormatException(s"Invalid suffix: $u")
        }
        unit.convert(value, if (u != null) timeSuffixes(u) else unit)
      case _ =>
        throw new NumberFormatException(s"Failed to parse byte string: $str")
    }
  }

  def timeStringAsUs(str: String): Long = timeStringAs(str, TimeUnit.MICROSECONDS)

  def timeStringAsMs(str: String): Long = timeStringAs(str, TimeUnit.MILLISECONDS)

  def timeStringAsSec(str: String): Long = timeStringAs(str, TimeUnit.SECONDS)

  def timeStringAsMin(str: String): Long = timeStringAs(str, TimeUnit.MINUTES)

  def timeStringAsHour(str: String): Long = timeStringAs(str, TimeUnit.HOURS)

  def timeStringAsDay(str: String): Long = timeStringAs(str, TimeUnit.DAYS)

}
