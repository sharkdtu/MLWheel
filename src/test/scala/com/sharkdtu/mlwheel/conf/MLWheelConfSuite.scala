package com.sharkdtu.mlwheel.conf

import com.sharkdtu.mlwheel.MLWheelFunSuite


class MLWheelConfSuite extends MLWheelFunSuite {

  test("loadDefaults") {
    sys.props.put("mlwheel.hello.world", "nihao")
    sys.props.put("mlwheel.hello.scala", "scala")

    val conf = new MLWheelConf(true)

    assert(conf.get("mlwheel.hello.world") == "nihao")
    assert(conf.get("mlwheel.hello.scala") == "scala")
  }

  test("set/get") {
    val conf = new MLWheelConf(false)

    conf.set("mlwheel.hello.world", "nihao")
    conf.set("mlwheel.hello.scala", "scala")
    assert(conf.get("mlwheel.hello.world") == "nihao")
    assert(conf.get("mlwheel.hello.scala") == "scala")
    assert(conf.get("mlwheel.hello.java", "java") == "java")
  }

  test("ConfigEntry") {
    val conf = new MLWheelConf(false)

    val entry1 = ConfigBuilder("mlwheel.hello.world").stringConf.createWithDefault("nihao")
    val entry2 = ConfigBuilder("mlwheel.hello.scala").stringConf.createWithDefault("scala")
    val entry3 = ConfigBuilder("mlwheel.hello.int").intConf.createWithDefault(100)
    val entry4 = ConfigBuilder("mlwheel.hello.long").internal().longConf.createWithDefault(100L)
    val entry5 = ConfigBuilder("mlwheel.hello.double").internal().doubleConf.createWithDefaultString("123.456")
    val entry6 = ConfigBuilder("mlwheel.hello.boolean").internal().booleanConf.createWithDefaultString("false")

    conf.set(entry1, "nihao2")
    conf.set(entry3, 123)
    conf.set("mlwheel.hello.long", "456")
    conf.set(entry5, 12.789)

    assert(conf.get(entry1) == "nihao2")
    assert(conf.get(entry2) == "scala")
    assert(conf.get(entry3) == 123)
    assert(conf.get(entry4) == 456L)
    assert(conf.get(entry5) == 12.789)
    assert(!conf.get(entry6))
  }

}
