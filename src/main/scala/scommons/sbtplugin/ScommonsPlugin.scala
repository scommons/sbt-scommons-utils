package scommons.sbtplugin

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.util.ResourcesUtils

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin

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
      "org.scommons.client" % "scommons-client-ui" % "*"
    ),

    sjsStageSettings(fastOptJS, Compile),
    sjsStageSettings(fullOptJS, Compile),
    sjsStageSettings(fastOptJS, Test),
    sjsStageSettings(fullOptJS, Test)
  )

  private def sjsStageSettings(sjsStage: TaskKey[Attributed[File]], config: ConfigKey) = {
    sjsStage in config := {
      copyWebpackResources(
        streams.value.log,
        (crossTarget in (config, sjsStage)).value,
        (fullClasspath in config).value,
        scommonsResourcesFileFilter.value,
        scommonsResourcesArtifacts.value
      )
      (sjsStage in config).value
    }
  }

  private def copyWebpackResources(log: Logger,
                                   webpackDir: File,
                                   cp: Seq[Attributed[File]],
                                   fileFilter: FileFilter,
                                   includeArtifacts: Seq[ModuleID]): Unit = {

    ResourcesUtils.extractFromClasspath(msg => log.info(msg), webpackDir, cp, fileFilter, includeArtifacts)
  }
}
