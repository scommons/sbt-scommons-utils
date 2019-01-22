package scommons.sbtplugin.project

import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

trait CommonMobileModule extends CommonModule {

  def scommonsReactNativeVersion: String

  override def definition: Project = {
    super.definition
      .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
      .settings(CommonMobileModule.settings: _*)
      .settings(
        scalaJSUseMainModuleInitializer := false,
        webpackBundlingMode := BundlingMode.LibraryOnly(),

        webpackConfigFile in Test := Some(
          baseDirectory.value / "src" / "test" / "resources" / "test.webpack.config.js"
        )
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-react", "scommons-react-core", None),
    ("scommons-react-native", "scommons-react-native-core", None),

    ("scommons-react", "scommons-react-test", Some("test")),
    ("scommons-react-native", "scommons-react-native-test", Some("test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.react-native" %%% "scommons-react-native-core" % scommonsReactNativeVersion
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq[ModuleID](
    "org.scommons.react-native" %%% "scommons-react-native-test" % scommonsReactNativeVersion
  ).map(_  % "test"))
}

object CommonMobileModule {

  val settings: Seq[Setting[_]] = Seq(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    
    //Opt-in @ScalaJSDefined by default
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    
    // react-native DO NOT require DOM, but we enable it here only to trigger the webpack build
    // since we substitute references to react-native module with our custom react-native-mocks module
    // inside the sc-react-native-mocks.webpack.config.js
    requiresDOM in Test := true,

    version in webpack := "3.5.5",
    
    emitSourceMaps := false,

    npmDependencies in Compile ++= Seq(
      "react" -> "^16.6.3",
      "react-dom" -> "^16.6.3" //TODO: remove dependency on react-dom
    ),
    npmResolutions in Compile ++= Map(
      "react" -> "^16.6.3",
      "react-dom" -> "^16.6.3"
    ),

    npmResolutions in Test ++= Map(
      "react" -> "^16.6.3",
      "react-dom" -> "^16.6.3"
    ),

    ideExcludedDirectories ++= {
      val base = baseDirectory.value
      List(
        base / "build",
        base / "node_modules"
      )
    },
    cleanKeepFiles ++= Seq(
      target.value / "scala-2.12" / "scalajs-bundler" / "main" / "node_modules",
      target.value / "scala-2.12" / "scalajs-bundler" / "test" / "node_modules",
      target.value / "scalajs-bundler-jsdom" / "node_modules"
    )
  )
}
