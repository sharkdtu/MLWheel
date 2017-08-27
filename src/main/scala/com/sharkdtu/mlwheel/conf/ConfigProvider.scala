package com.sharkdtu.mlwheel.conf

import java.util.{Map => JMap}

/**
 * A source of configuration values.
 */
private[mlwheel] trait ConfigProvider {

  def get(key: String): Option[String]

}

private[mlwheel] class EnvProvider extends ConfigProvider {

  override def get(key: String): Option[String] = sys.env.get(key)

}

private[mlwheel] class SystemProvider extends ConfigProvider {

  override def get(key: String): Option[String] = sys.props.get(key)

}

private[mlwheel] class MapProvider(conf: JMap[String, String]) extends ConfigProvider {

  override def get(key: String): Option[String] = Option(conf.get(key))

}

/**
 * A config provider that only reads MLWheel config keys, and considers default values for known
 * configs when fetching configuration values.
 */
private[mlwheel] class MLWheelConfigProvider(conf: JMap[String, String]) extends ConfigProvider {

  import ConfigEntry._

  override def get(key: String): Option[String] = {
    if (key.startsWith("mlwheel.")) {
      Option(conf.get(key)).orElse(defaultValueString(key))
    } else {
      None
    }
  }

  private def defaultValueString(key: String): Option[String] = {
    findEntry(key) match {
      case e: ConfigEntryWithDefault[_] => Option(e.defaultValueString)
      case e: ConfigEntryWithDefaultString[_] => Option(e.defaultValueString)
      case e: FallbackConfigEntry[_] => get(e.fallback.key)
      case _ => None
    }
  }

}