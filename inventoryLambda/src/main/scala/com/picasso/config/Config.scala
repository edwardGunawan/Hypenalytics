package com.picasso.config

import cats.Applicative
import ciris.{ConfigValue, Effect, env}

case class Config(tableName: String)

object Config {

  val container: ConfigValue[Effect, Config] = env("tableName").map { tableName =>
    Config(tableName)
  }
}
