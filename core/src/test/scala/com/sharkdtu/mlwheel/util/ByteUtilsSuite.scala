package com.sharkdtu.mlwheel.util

import com.sharkdtu.mlwheel.PSFunSuite


class ByteUtilsSuite extends PSFunSuite {

  test("byteStringAsByte") {
    assert(ByteUtils.byteStringAsByte("4") == 4L)
    assert(ByteUtils.byteStringAsByte("4Byte") == 4L)
    assert(ByteUtils.byteStringAsByte("4KB") == (4L << 10))
    assert(ByteUtils.byteStringAsByte("4MB") == (4L << 20))
    assert(ByteUtils.byteStringAsByte("4GB") == (4L << 30))
    assert(ByteUtils.byteStringAsByte("4TB") == (4L << 40))
    assert(ByteUtils.byteStringAsByte("4PB") == (4L << 50))
  }

  test("byteStringAsKB") {
    assert(ByteUtils.byteStringAsKB("4") == 4L)
    assert(ByteUtils.byteStringAsKB(s"${4L << 10}Byte") == 4L)
    assert(ByteUtils.byteStringAsKB("4KB") == 4L)
    assert(ByteUtils.byteStringAsKB("4MB") == (4L << 10))
    assert(ByteUtils.byteStringAsKB("4GB") == (4L << 20))
    assert(ByteUtils.byteStringAsKB("4TB") == (4L << 30))
    assert(ByteUtils.byteStringAsKB("4PB") == (4L << 40))
  }

  test("byteStringAsMB") {
    assert(ByteUtils.byteStringAsMB("4") == 4L)
    assert(ByteUtils.byteStringAsMB(s"${4L << 20}Byte") == 4L)
    assert(ByteUtils.byteStringAsMB(s"${4L << 10}KB") == 4L)
    assert(ByteUtils.byteStringAsMB("4MB") == 4L)
    assert(ByteUtils.byteStringAsMB("4GB") == (4L << 10))
    assert(ByteUtils.byteStringAsMB("4TB") == (4L << 20))
    assert(ByteUtils.byteStringAsMB("4PB") == (4L << 30))
  }

  test("byteStringAsGB") {
    assert(ByteUtils.byteStringAsGB("4") == 4L)
    assert(ByteUtils.byteStringAsGB(s"${4L << 30}Byte") == 4L)
    assert(ByteUtils.byteStringAsGB(s"${4L << 20}KB") == 4L)
    assert(ByteUtils.byteStringAsGB(s"${4L << 10}MB") == 4L)
    assert(ByteUtils.byteStringAsGB("4GB") == 4L)
    assert(ByteUtils.byteStringAsGB("4TB") == (4L << 10))
    assert(ByteUtils.byteStringAsGB("4PB") == (4L << 20))
  }

  test("byteStringAsTB") {
    assert(ByteUtils.byteStringAsTB("4") == 4L)
    assert(ByteUtils.byteStringAsTB(s"${4L << 40}Byte") == 4L)
    assert(ByteUtils.byteStringAsTB(s"${4L << 30}KB") == 4L)
    assert(ByteUtils.byteStringAsTB(s"${4L << 20}MB") == 4L)
    assert(ByteUtils.byteStringAsTB(s"${4L << 10}GB") == 4L)
    assert(ByteUtils.byteStringAsTB("4TB") == 4L)
    assert(ByteUtils.byteStringAsTB("4PB") == (4L << 10))
  }

  test("byteStringAsPB") {
    assert(ByteUtils.byteStringAsPB("4") == 4L)
    assert(ByteUtils.byteStringAsPB(s"${4L << 50}Byte") == 4L)
    assert(ByteUtils.byteStringAsPB(s"${4L << 40}KB") == 4L)
    assert(ByteUtils.byteStringAsPB(s"${4L << 30}MB") == 4L)
    assert(ByteUtils.byteStringAsPB(s"${4L << 20}GB") == 4L)
    assert(ByteUtils.byteStringAsPB(s"${4L << 10}TB") == 4L)
    assert(ByteUtils.byteStringAsPB("4PB") == 4L)
  }

  // Test invalid strings
  intercept[NumberFormatException] {
    ByteUtils.byteStringAsKB("600l")
  }

  intercept[NumberFormatException] {
    ByteUtils.byteStringAsKB("This breaks 600KB")
  }

  intercept[NumberFormatException] {
    ByteUtils.byteStringAsKB("This breaks 600HH")
  }

  intercept[NumberFormatException] {
    ByteUtils.byteStringAsKB("600EB This breaks")
  }

  intercept[NumberFormatException] {
    ByteUtils.byteStringAsKB("This 123PB breaks")
  }

}
