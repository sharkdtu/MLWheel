package com.sharkdtu.mlwheel.serializer

import com.sharkdtu.mlwheel.PSFunSuite
import com.sharkdtu.mlwheel.conf.PSConf

class JavaSerializerSuite extends PSFunSuite {
  test("JavaSerializer instances are serializable") {
    val serializer = new JavaSerializer(new PSConf())
    val instance = serializer.newInstance()
    val obj = instance.deserialize[JavaSerializer](instance.serialize(serializer))
    // enforce class cast
    obj.getClass
  }
}