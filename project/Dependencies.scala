import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.2.8"

  object Cats {
    private val version = "2.6.1"
    val core = "org.typelevel" %% "cats-core" % version
    val kernel = "org.typelevel" %% "cats-kernel" % version
  }

  object CatsEffect {
    private val version = "3.1.0"
    val core = "org.typelevel" %% "cats-effect" % version
  }

  object CatsMTL {
    private val version = "1.2.1"
    val core = "org.typelevel" %% "cats-mtl" % version
  }

  object Ciris {
    private val version = "2.1.1"
    val core: ModuleID = "is.cir" %% "ciris" % version
    val refined: ModuleID = "is.cir" %% "ciris-refined" % version
  }

  object Circe {
    private val version = "0.14.1"
    val core = "io.circe" %% "circe-core" % version
    val generic = "io.circe" %% "circe-generic" % version
    val genericExtra = "io.circe" %% "circe-generic-extras" % version
    val parser: ModuleID = "io.circe" %% "circe-parser" % version
    val config: ModuleID = "io.circe" %% "circe-config" % "0.8.0"
  }

  object AWS {
    private val version = "2.16.93"
    val core: ModuleID = "software.amazon.awssdk" % "core" % version
    val sts: ModuleID = "software.amazon.awssdk" % "sts" % version
    val dynamodb: ModuleID = "software.amazon.awssdk" % "dynamodb" % version
  }

  object AWSLambdaRuntime {
    val core: ModuleID = "com.amazonaws" % "aws-lambda-java-core" % "1.2.1"
    val event: ModuleID = "com.amazonaws" % "aws-lambda-java-events" % "3.8.0"
  }

  object HTTP4s {
    private val version = "1.0.0-M25"
    val client: ModuleID = "org.http4s" %% "http4s-blaze-client" % version
  }

  object ScalaMock {
    private val version = "5.1.0"
    val core = "org.scalamock" %% "scalamock" % version
  }

  object Dynosaur {
    private val version = "0.3.0"
    val core = "org.systemfw" %% "dynosaur-core" % version
  }

  object FS2AWS {
    private val version = "3.1.1"
    val core = "io.laserdisc" %% "fs2-aws" % version
    val dynamodb = "io.laserdisc" %% "fs2-aws-dynamodb" % "4.0.0-RC2"
  }

  object Meteor {
    private val version = "1.0.7"
    val core = "io.github.d2a4u" %% "meteor-awssdk" % version
  }

  object TestLib {
    val mockItoCats = "org.mockito" %% "mockito-scala-cats" % "1.16.27"
    val testContainerScala = "com.dimafeng" %% "testcontainers-scala" % "0.38.9"
    val testContainersMockServer = "org.testcontainers" % "mockserver" % "1.16.0"
    val testcontainers = "org.testcontainers" % "testcontainers" % "1.16.0"
    val mockServerNetty = "org.mock-server" % "mockserver-netty" % "5.11.2"
  }

}
