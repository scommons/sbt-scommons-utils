package scommons.sbtplugin.project

import com.typesafe.sbt.web.SbtWeb
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories
import scoverage.ScoverageKeys._
import webscalajs.ScalaJSWeb

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait CommonClientModule extends CommonModule {

  def scommonsNodejsVersion: String
  def scommonsReactVersion: String
  def scommonsClientVersion: String

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalaJSWeb, SbtWeb)
      .settings(CommonClientModule.settings: _*)
      .settings(
        coverageExcludedPackages := ".*Css",

        scalaJSUseMainModuleInitializer := true,
        webpackBundlingMode := BundlingMode.LibraryOnly(),
        
        //dev
        webpackConfigFile in fastOptJS := Some(baseDirectory.value / "client.webpack.config.js"),
        //production
        webpackConfigFile in fullOptJS := Some(baseDirectory.value / "client.webpack.config.js"),
        //reload workflow and tests
        scommonsRequireWebpackInTest := true,
        webpackConfigFile in Test := Some(baseDirectory.value / "test.webpack.config.js")
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-client", "scommons-client-ui", None),
    ("scommons-react", "scommons-react-core", None),
    ("scommons-react", "scommons-react-dom", None),
    ("scommons-react", "scommons-react-redux", None),
    
    ("scommons-nodejs", "scommons-nodejs-test", Some("test")),
    ("scommons-react", "scommons-react-test", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.client" %%% "scommons-client-ui" % scommonsClientVersion
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.nodejs" %%% "scommons-nodejs-test" % scommonsNodejsVersion,
    "org.scommons.react" %%% "scommons-react-test" % scommonsReactVersion
  ).map(_  % "test"))
}

object CommonClientModule {

  val settings: Seq[Setting[_]] = Seq(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    
    //Opt-in @ScalaJSDefined by default
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    requireJsDomEnv in Test := false,
    version in webpack := "4.29.0",
    emitSourceMaps := false,

    npmDependencies in Compile ++= Seq(
      "react" -> "^16.8.0",
      "react-dom" -> "^16.8.0"
    ),
    npmResolutions in Compile ++= Map(
      "react" -> "^16.8.0",
      "react-dom" -> "^16.8.0"
    ),
    
    npmResolutions in Test ++= Map(
      "react" -> "^16.8.0",
      "react-dom" -> "^16.8.0"
    ),

    ideExcludedDirectories ++= {
      val base = baseDirectory.value
      List(
        base / "build",
        base / "node_modules"
      )
    }
  )
}
