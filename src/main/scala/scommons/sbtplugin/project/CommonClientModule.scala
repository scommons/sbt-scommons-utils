package scommons.sbtplugin.project

import com.typesafe.sbt.web.SbtWeb
import org.sbtidea.SbtIdeaPlugin.ideaExcludeFolders
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys._
import webscalajs.ScalaJSWeb

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait CommonClientModule extends CommonModule {

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
        webpackConfigFile in Test := Some(baseDirectory.value / "test.webpack.config.js")
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-client", "scommons-client-ui", None),
    ("scommons-react", "scommons-react-core", None),
    ("scommons-react", "scommons-react-test-dom", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.client" %%% "scommons-client-ui" % scommonsClientVersion
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.react" %%% "scommons-react-test-dom" % scommonsReactVersion
  ).map(_  % "test"))
}

object CommonClientModule {

  val settings: Seq[Setting[_]] = Seq(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    
    //Opt-in @ScalaJSDefined by default
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    requiresDOM in Test := true,
    version in webpack := "3.5.5",
    emitSourceMaps := false,

    npmDependencies in Compile ++= Seq(
      "react" -> "^16.6.3",
      "react-dom" -> "^16.6.3"
    ),
    npmResolutions in Compile ++= Map(
      "react" -> "^16.6.3",
      "react-dom" -> "^16.6.3"
    ),
    
    npmResolutions in Test ++= Map(
      "react" -> "^16.6.3",
      "react-dom" -> "^16.6.3"
    ),

    ideaExcludeFolders ++= {
      val base = baseDirectory.value
      List(
        s"$base/build",
        s"$base/node_modules"
      )
    },
    cleanKeepFiles ++= Seq(
      target.value / "scala-2.12" / "scalajs-bundler" / "main" / "node_modules",
      target.value / "scala-2.12" / "scalajs-bundler" / "test" / "node_modules",
      target.value / "scalajs-bundler-jsdom" / "node_modules"
    )
  )
}
