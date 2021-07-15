package scommons.sbtplugin

import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt._
import webscalajs.WebScalaJS.autoImport._

import scalajsbundler.sbtplugin.WebScalaJSBundlerPlugin

object WebpackAssetsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires = WebScalaJSBundlerPlugin

  object autoImport {
    /**
      * Sequence of webpack output PathMapping’s to include to sbt-web’s assets.
      *
      * It depends on webpack task which in turn depends on scalajs task.
      * Thus this task is scoped by a scalajs task (`fastOptJS` or `fullOptJS`):
      * {{{
      *   webpackAssets in fastOptJS ++= WebpackAssets.ofProject(fastOptJS, clientProject) { build => (build / "styles").*** }.value,
      *   webpackAssets in fullOptJS ++= WebpackAssets.ofProject(fullOptJS, clientProject) { build => (build / "styles").*** }.value
      * }}}
      * 
      * or for all specified `scalaJSProjects`:
      * {{{
      *   webpackAssets in fastOptJS ++= WebpackAssets.ofScalaJSProjects(fastOptJS) { build => (build / "styles").*** }.value,
      *   webpackAssets in fullOptJS ++= WebpackAssets.ofScalaJSProjects(fullOptJS) { build => (build / "styles").*** }.value
      * }}}
      *
      * @see [[scommons.sbtplugin.WebpackAssets.ofProject]]
      */
    val webpackAssets: TaskKey[Seq[PathMapping]] = taskKey[Seq[PathMapping]](
      "Assets (resources that are not CommonJS modules) produced by Webpack"
    )

    val WebpackAssets = scommons.sbtplugin.WebpackAssets
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    fastOptJS / webpackAssets := Nil,
    fullOptJS / webpackAssets := Nil,
    scalaJSDev := pipelineStage(Compile / fastOptJS, scalaJSDev).value,
    scalaJSProd := pipelineStage(Compile / fullOptJS, scalaJSProd).value
  )

  def pipelineStage(sjsStage: TaskKey[Attributed[File]],
                    self: TaskKey[Pipeline.Stage]): Def.Initialize[Task[Pipeline.Stage]] = Def.taskDyn {

    val scalajsMappings = WebScalaJSBundlerPlugin.pipelineStage(sjsStage, self).value
    val webpackMappings = (sjsStage / webpackAssets).value

    Def.task { mappings: Seq[PathMapping] =>
      scalajsMappings(mappings) ++ webpackMappings
    }
  }
}
