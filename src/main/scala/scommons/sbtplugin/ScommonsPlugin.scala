package scommons.sbtplugin

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.util.ResourcesHelper

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ScommonsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires = ScalaJSBundlerPlugin

  object autoImport {
    val scommonsResourcesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
      "File filter of resources files, that should be automatically copied/extracted to the webpack directory"
    )
    val scommonsResourcesArtifacts: SettingKey[Seq[ModuleID]] = settingKey[Seq[ModuleID]](
      "List of artifacts (JARs) with resources, that should be automatically extracted to the webpack directory"
    )
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    scommonsResourcesFileFilter :=
      "*.css" ||
        "*.ico" ||
        "*.png" ||
        "*.jpg" ||
        "*.jpeg" ||
        "*.gif",
    scommonsResourcesArtifacts := Seq(
      "org.scommons" %%% "scommons-client" % "*"
    ),
    npmUpdate in Compile := {
      copyWebpackResources(
        (npmUpdate in Compile).value,
        (fullClasspath in Compile).value,
        scommonsResourcesFileFilter.value,
        scommonsResourcesArtifacts.value
      )
    },
    npmUpdate in Test := {
      copyWebpackResources(
        (npmUpdate in Test).value,
        (fullClasspath in Test).value,
        scommonsResourcesFileFilter.value,
        scommonsResourcesArtifacts.value
      )
    }
  )

  private def copyWebpackResources(webpackDir: File,
                                   cp: Seq[Attributed[File]],
                                   fileFilter: FileFilter,
                                   resourcesArtifacts: Seq[ModuleID]): File = {

    ResourcesHelper.extractFromClasspath(webpackDir, cp, fileFilter)
    webpackDir
  }
}
