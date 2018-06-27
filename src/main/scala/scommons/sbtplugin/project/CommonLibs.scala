package scommons.sbtplugin.project

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._

trait CommonLibs {

  val akkaVersion = "2.5.6"
  val playVer = "2.6.7" // should be the same as Play sbt-plugin version (see build.sbt)

  //////////////////////////////////////////////////////////////////////////////
  // jvm dependencies

  lazy val play = Def.setting("com.typesafe.play" %% "play" % playVer)
  lazy val scaldiPlay = Def.setting("org.scaldi" %% "scaldi-play" % "0.5.17")

  lazy val logback = Def.setting("ch.qos.logback" % "logback-classic" % "1.1.7")
  lazy val slf4jApi = Def.setting("org.slf4j" % "slf4j-api" % "1.7.12")
  lazy val jclOverSlf4j = Def.setting("org.slf4j" % "jcl-over-slf4j" % "1.7.12")
  lazy val log4jToSlf4j = Def.setting("org.apache.logging.log4j" % "log4j-to-slf4j" % "2.2")

  lazy val swaggerPlay = Def.setting("io.swagger" %% "swagger-play2" % "1.6.0")
  lazy val swaggerAnnotations = Def.setting("io.swagger" % "swagger-annotations" % "1.5.16")
  lazy val swaggerUi = Def.setting("org.webjars" % "swagger-ui" % "2.2.2")

  //////////////////////////////////////////////////////////////////////////////
  // js dependencies

  lazy val scalajsDom = Def.setting("org.scala-js" %%% "scalajs-dom" % "0.9.2")
}

object CommonLibs extends CommonLibs
