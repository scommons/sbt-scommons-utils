package scommons.sbtplugin.project

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._

trait CommonTestLibs {

  val scalaTestVersion = "3.0.1"
  val scalaMockVersion = "3.6.0"

  val akkaVersion: String = CommonLibs.akkaVersion

  lazy val scalaTest = Def.setting("org.scalatest" %% "scalatest" % scalaTestVersion)
  lazy val scalaMock = Def.setting("org.scalamock" %% "scalamock-scalatest-support" % scalaMockVersion)

  lazy val mockito = Def.setting("org.mockito" % "mockito-all" % "1.9.5")

  lazy val scalaTestPlusPlay = Def.setting("org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2")
  lazy val akkaStreamTestKit = Def.setting("com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion)

  // Scala.js dependencies

  lazy val scalaTestJs = Def.setting("org.scalatest" %%% "scalatest" % scalaTestVersion)
  lazy val scalaMockJs = Def.setting("org.scalamock" %%% "scalamock-scalatest-support" % scalaMockVersion)
}

object CommonTestLibs extends CommonTestLibs
