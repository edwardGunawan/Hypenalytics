package com.picasso
import sbt._

object CompilerPlugins {

  object KindProjector {
    val core: ModuleID = "org.typelevel" %% "kind-projector" % "0.13.1" cross CrossVersion.full // you have to put in cross version or else it won't show up
  }

  object BetterMonadicFor {
    val core: ModuleID = "com.olegpy" %% "better-monadic-for" % "0.3.1"
  }
}
