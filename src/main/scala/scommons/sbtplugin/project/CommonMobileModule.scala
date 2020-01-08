package scommons.sbtplugin.project

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.ScommonsPlugin.autoImport._
import scommons.sbtplugin.project.CommonModule.ideExcludedDirectories

import scalajsbundler.ExternalCommand
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

    scommonsResourcesArtifacts := Seq(
      "org.scommons.react" % "scommons-react-core" % "*",
      "org.scommons.react-native" % "scommons-react-native-test" % "*"
    ),

    scalaJSModuleKind := ModuleKind.CommonJSModule,
    
    //Opt-in @ScalaJSDefined by default
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    
    // react-native DO NOT require DOM, but we enable it here only to trigger the webpack build
    // since we substitute references to react-native module with our custom react-native-mocks module
    // inside the sc-react-native-mocks.webpack.config.js
    requireJsDomEnv in Test := true,
    installJsdom := {
      val jsdomVersion = (version in installJsdom).value

      val installDir = target.value / "scalajs-bundler-jsdom"
      val baseDir = baseDirectory.value
      val jsdomDir = installDir / "node_modules" / "jsdom"
      val log = streams.value.log
      if (!jsdomDir.exists()) {
        log.info(s"Installing jsdom into: ${installDir.absolutePath}")
        IO.createDirectory(installDir / "node_modules")
        ExternalCommand.addPackages(
          baseDir,
          installDir,
          useYarn.value,
          log,
          npmExtraArgs.value,
          yarnExtraArgs.value
        )(s"jsdom@$jsdomVersion")
      }
      installDir
    },

    version in webpack := "3.5.5",
    
    emitSourceMaps := false,

    npmDependencies in Compile ++= Seq(
      "react" -> "^16.8.0",
      "react-dom" -> "^16.8.0" //TODO: remove dependency on react-dom
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
        base / "android" / "build",
        base / "ios" / "build",
        base / "ios" / "Pods",
        base / ".expo",
        base / "node_modules"
      )
    }
  )
}
