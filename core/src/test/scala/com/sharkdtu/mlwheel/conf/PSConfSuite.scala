package com.sharkdtu.mlwheel.conf

import java.util.concurrent.TimeUnit

import com.sharkdtu.mlwheel.PSFunSuite
import com.sharkdtu.mlwheel.util.ByteUnit


class PSConfSuite extends PSFunSuite {

  test("loadDefaults") {
    sys.props.put("ps.hello.world", "nihao")
    sys.props.put("ps.hello.scala", "scala")

    val conf = new PSConf(true)

    assert(conf.get("ps.hello.world") == "nihao")
    assert(conf.get("ps.hello.scala") == "scala")
  }

  test("set/get") {
    val conf = new PSConf(false)

    conf.set("ps.hello.world", "nihao")
    conf.set("ps.hello.scala", "scala")
    assert(conf.get("ps.hello.world") == "nihao")
    assert(conf.get("ps.hello.scala") == "scala")
    assert(conf.get("ps.hello.java", "java") == "java")
  }

  test("ConfigEntry") {
    val conf = new PSConf(false)

    val entry1 =
      ConfigBuilder("ps.hello.world")
        .stringConf
        .createWithDefault("nihao")
    val entry2 = ConfigBuilder("ps.hello.scala")
        .stringConf
        .createWithDefault("scala")
    val entry3 =
      ConfigBuilder("ps.hello.int")
        .intConf
        .createWithDefault(100)
    val entry4 =
      ConfigBuilder("ps.hello.long")
        .internal()
        .longConf
        .createWithDefault(100L)
    val entry5 =
      ConfigBuilder("ps.hello.double")
        .internal()
        .doubleConf
        .createWithDefaultString("123.456")
    val entry6 =
      ConfigBuilder("ps.hello.boolean")
        .internal()
        .booleanConf
        .createWithDefaultString("false")
    val entry7 =
      ConfigBuilder("ps.hello.time")
        .timeConf(TimeUnit.SECONDS)
        .createWithDefaultString("1000ms")
    val entry8 =
      ConfigBuilder("ps.hello.byte")
        .byteConf(ByteUnit.KB)
        .createWithDefaultString("4MB")

    conf.set(entry1, "nihao2")
    conf.set(entry3, 123)
    conf.set("ps.hello.long", "456")
    conf.set(entry5, 12.789)

    assert(conf.get(entry1) == "nihao2")
    assert(conf.get(entry2) == "scala")
    assert(conf.get(entry3) == 123)
    assert(conf.get(entry4) == 456L)
    assert(conf.get(entry5) == 12.789)
    assert(!conf.get(entry6))
    assert(conf.get(entry7) == 1L)
    assert(conf.get(entry8) == 4L * 1024)
  }

}
