import com.picasso.CompilerPlugins.{KindProjector}
import Dependencies._
import sbtassembly.{AssemblyKeys => assemblykeys}
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.moduleFilterRemoveValue

lazy val commonSettings = Seq(
  scalaVersion := { "2.13.6" },
  organization := { "picasso" },
  dependencyUpdatesFilter -= moduleFilter(name = "scala-library"),
  dependencyUpdatesFailBuild := { false },
  // libraryDependencySchemes += "org.typelevel" %% "cats-effect" % "always",
  scalacOptions ++= Seq(
    "-encoding",
//    "-Ylog-classpath",
    "UTF-8", // source files are in UTF-8
    "-deprecation", // warn about use of deprecated APIs
    "-unchecked", // warn about unchecked type parameters
    "-feature", // warn about misused language features
    "-language:higherKinds", // allow higher kinded types without "import scala.language.higherKinds"
    "-language:implicitConversions", // allow use of implicit conversions
    "-language:postfixOps",
    "-Xlint", // enabled handy linter warnings
    "-Ymacro-annotations", // macro annotation enabled
    "-Ywarn-macros:after" // allows the compiler to resolve implicit imports being flagged as unused
//    "-Xfatal-warnings" // promotes the warnings to compiler error (so it cannot be ignored)
  ),
  addCompilerPlugin(KindProjector.core)
)

lazy val root = (project in file(".")).aggregate(inventoryLambda)

lazy val core = (project in file("core")).settings(
  name := "core",
  commonSettings,
  libraryDependencies ++= Seq(
    AWS.core,
    AWS.sts,
    AWS.dynamodb,
    Cats.core,
    CatsEffect.core,
    Circe.core,
    Circe.generic,
    Circe.genericExtra,
    Circe.parser,
    Circe.config,
    Dynosaur.core,
    Meteor.core
  ) ++ Seq(
    scalaTest,
    ScalaMock.core
  ).map(_ % "test")
)

lazy val inventoryLambda = (project in file("inventoryLambda"))
  .dependsOn(core)
  .settings(
    name := "InventoryLambda",
    version := "0.1.0-SNAPSHOT",
    commonSettings,
    assemblySettings,
    libraryDependencies ++= Seq(
      AWS.core,
      AWS.sts,
      AWSLambdaRuntime.core,
      AWSLambdaRuntime.event,
      Cats.core,
      CatsEffect.core,
      Circe.core,
      Circe.generic,
      Circe.genericExtra,
      Circe.parser,
      Circe.config,
      Ciris.core,
      Ciris.refined
    ) ++ Seq(
      ScalaMock.core,
      scalaTest
    ).map(_ % "test")
  )

lazy val assemblySettings = Seq(
  test in assemblykeys.assembly := {},
  assemblykeys.assemblyJarName in assemblykeys.assembly := name.value + ".jar",
  assemblykeys.assemblyMergeStrategy in assemblykeys.assembly := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.first
    case "module-info.class" => MergeStrategy.discard
    case "application.conf" => MergeStrategy.concat
    case "reference.conf" => MergeStrategy.concat
    case "deriving.conf" => MergeStrategy.concat
    case PathList("io", "netty", _ @_*) => MergeStrategy.first
    case PathList(ps @ _*)
        if Set(
          "service-2.json",
          "waiters-2.json",
          "customization.config",
          "paginators-1.json",
          "module-info.class",
          "mime.types"
        ).contains(ps.last) =>
      MergeStrategy.discard
    case x => MergeStrategy.first
  }
)
