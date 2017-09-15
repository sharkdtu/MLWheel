package com.sharkdtu.mlwheel.util

import com.sharkdtu.mlwheel.PSFunSuite


class TimeUtilsSuite extends PSFunSuite {

  test("timeStringAsUs") {
    assert(TimeUtils.timeStringAsUs("4") == 4L)
    assert(TimeUtils.timeStringAsUs("4us") == 4L)
    assert(TimeUtils.timeStringAsUs("4ms") == 4 * 1000L)
    assert(TimeUtils.timeStringAsUs("4s") == 4 * 1000000L)
    assert(TimeUtils.timeStringAsUs("4min") == 4 * 60 * 1000000L)
    assert(TimeUtils.timeStringAsUs("4h") == 4 * 60 * 60 * 1000000L)
    assert(TimeUtils.timeStringAsUs("4d") == 4 * 24 * 60 * 60 * 1000000L)
  }

  test("timeStringAsMs") {
    assert(TimeUtils.timeStringAsMs("4") == 4L)
    assert(TimeUtils.timeStringAsMs(s"${4 * 1000L}us") == 4L)
    assert(TimeUtils.timeStringAsMs("4ms") == 4L)
    assert(TimeUtils.timeStringAsMs("4s") == 4 * 1000L)
    assert(TimeUtils.timeStringAsMs("4min") == 4 * 60 * 1000L)
    assert(TimeUtils.timeStringAsMs("4h") == 4 * 60 * 60 * 1000L)
    assert(TimeUtils.timeStringAsMs("4d") == 4 * 24 * 60 * 60 * 1000L)
  }

  test("timeStringAsSec") {
    assert(TimeUtils.timeStringAsSec("4") == 4L)
    assert(TimeUtils.timeStringAsSec(s"${4 * 1000000L}us") == 4L)
    assert(TimeUtils.timeStringAsSec(s"${4 * 1000L}ms") == 4L)
    assert(TimeUtils.timeStringAsSec("4s") == 4L)
    assert(TimeUtils.timeStringAsSec("4min") == 4 * 60L)
    assert(TimeUtils.timeStringAsSec("4h") == 4 * 60 * 60L)
    assert(TimeUtils.timeStringAsSec("4d") == 4 * 24 * 60 * 60L)
  }

  test("timeStringAsMin") {
    assert(TimeUtils.timeStringAsMin("4") == 4L)
    assert(TimeUtils.timeStringAsMin(s"${4 * 60 * 1000000L}us") == 4L)
    assert(TimeUtils.timeStringAsMin(s"${4 * 60 * 1000L}ms") == 4L)
    assert(TimeUtils.timeStringAsMin(s"${4 * 60}s") == 4L)
    assert(TimeUtils.timeStringAsMin("4min") == 4L)
    assert(TimeUtils.timeStringAsMin("4h") == 4 * 60L)
    assert(TimeUtils.timeStringAsMin("4d") == 4 * 24 * 60L)
  }

  test("timeStringAsHour") {
    assert(TimeUtils.timeStringAsHour("4") == 4L)
    assert(TimeUtils.timeStringAsHour(s"${4 * 60 * 60 * 1000000L}us") == 4L)
    assert(TimeUtils.timeStringAsHour(s"${4 * 60 * 60 * 1000L}ms") == 4L)
    assert(TimeUtils.timeStringAsHour(s"${4 * 60 * 60L}s") == 4L)
    assert(TimeUtils.timeStringAsHour(s"${4 * 60L}min") == 4L)
    assert(TimeUtils.timeStringAsHour("4h") == 4L)
    assert(TimeUtils.timeStringAsHour("4d") == 4 * 24L)
  }

  test("timeStringAsDay") {
    assert(TimeUtils.timeStringAsDay("4") == 4L)
    assert(TimeUtils.timeStringAsDay(s"${4 * 24 * 60 * 60 * 1000000L}us") == 4L)
    assert(TimeUtils.timeStringAsDay(s"${4 * 24 * 60 * 60 * 1000L}ms") == 4L)
    assert(TimeUtils.timeStringAsDay(s"${4 * 24 * 60 * 60L}s") == 4L)
    assert(TimeUtils.timeStringAsDay(s"${4 * 24 * 60L}min") == 4L)
    assert(TimeUtils.timeStringAsDay(s"${4 * 24L}h") == 4L)
    assert(TimeUtils.timeStringAsDay("4d") == 4L)
  }

  // Test invalid strings
  intercept[NumberFormatException] {
    TimeUtils.timeStringAsMs("600l")
  }

  intercept[NumberFormatException] {
    TimeUtils.timeStringAsMs("This breaks 600s")
  }

  intercept[NumberFormatException] {
    TimeUtils.timeStringAsMs("This breaks 600ds")
  }

  intercept[NumberFormatException] {
    TimeUtils.timeStringAsMs("600s This breaks")
  }

  intercept[NumberFormatException] {
    TimeUtils.timeStringAsMs("This 123s breaks")
  }

}
