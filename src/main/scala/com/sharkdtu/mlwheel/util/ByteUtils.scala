package com.sharkdtu.mlwheel.util

class ByteUnit(val multiplier: Long, val name: String) {
  import ByteUnit._

  // Convert the provided number (d) interpreted as this unit type to unit type (u).
  def convertTo(d: Long, u: ByteUnit): Long = {
    if (multiplier > u.multiplier) {
      val ratio = multiplier / u.multiplier
      if (Long.MaxValue / ratio < d) {
        throw new IllegalArgumentException(s"Conversion of $d exceeds Long.MAX_VALUE" +
          s"from $name to ${u.name}. Try a larger unit (e.g. MiB instead of KiB)")
      }
      d * ratio
    } else {
      // Perform operations in this order to avoid potential overflow
      // when computing d * multiplier
      d / (u.multiplier / multiplier)
    }
  }

  def convertFrom(d: Long, u: ByteUnit): Long = {
    u.convertTo(d, this)
  }

  def toBytes(d: Long): Long = {
    if (d < 0) {
      throw new IllegalArgumentException(s"Negative size value. Size must be positive: $d")
    }
    d * multiplier
  }

  def tiKB(d: Long): Long = convertTo(d, KB)
  def tiMB(d: Long): Long = convertTo(d, MB)
  def tiGB(d: Long): Long = convertTo(d, GB)
  def tiTB(d: Long): Long = convertTo(d, TB)
  def tiPB(d: Long): Long = convertTo(d, PB)

}

object ByteUnit {

  private def apply(multiplier: Long, name: String): ByteUnit = {
    new ByteUnit(multiplier, name)
  }

  val BYTE = ByteUnit(1, "Byte")
  val KB = ByteUnit(1024L, "KB")
  val MB = ByteUnit(math.pow(1024L, 2L).toLong, "MB")
  val GB = ByteUnit(math.pow(1024L, 3L).toLong, "GB")
  val TB = ByteUnit(math.pow(1024L, 4L).toLong, "TB")
  val PB = ByteUnit(math.pow(1024L, 5L).toLong, "PB")

}

object ByteUtils {

  private val byteSuffixes = Map(
    "Byte" -> ByteUnit.BYTE,
    "KB" -> ByteUnit.KB,
    "MB" -> ByteUnit.MB,
    "GB" -> ByteUnit.GB,
    "TB" -> ByteUnit.TB,
    "TB" -> ByteUnit.PB)

  /**
   * Convert a passed byte string (e.g. 50b, 100kb, or 250mb) to the given. If no suffix is
   * provided, a direct conversion to the provided unit is attempted.
   */
  def byteStringAs(str: String, unit: ByteUnit): Long = {
    val lower = str.toLowerCase.trim

    val pattern = "([0-9]+)([a-z]+)?".r
    lower match {
      case pattern(d, u) =>
        val value = d.toLong
        if (u != null && !byteSuffixes.contains(u)) {
          throw new NumberFormatException(s"Invalid suffix: $u")
        }
        unit.convertFrom(value, if (u != null) byteSuffixes(u) else unit)
      case _ =>
        throw new NumberFormatException(s"Failed to parse byte string: $str")
    }
  }

  def byteStringAsBytes(str: String): Long = byteStringAs(str, ByteUnit.BYTE)

  def byteStringAsKB(str: String): Long = byteStringAs(str, ByteUnit.KB)

  def byteStringAsMB(str: String): Long = byteStringAs(str, ByteUnit.MB)

  def byteStringAsGB(str: String): Long = byteStringAs(str, ByteUnit.GB)

  def byteStringAsTB(str: String): Long = byteStringAs(str, ByteUnit.TB)

  def byteStringAsPB(str: String): Long = byteStringAs(str, ByteUnit.PB)

}
