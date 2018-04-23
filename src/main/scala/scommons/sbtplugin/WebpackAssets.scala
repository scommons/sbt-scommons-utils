package scommons.sbtplugin

import com.typesafe.sbt.web.PathMapping
import sbt._

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object WebpackAssets {

  def ofProject(sjsStage: TaskKey[Attributed[File]], project: ProjectReference)
               (assets: File => PathFinder): Def.Initialize[Task[Seq[PathMapping]]] = Def.task {

    // resolve webpack assets after webpack task is finished
    (webpack in (project, Compile, sjsStage in project)).value

    val webpackBuildDir = (npmUpdate in (project, Compile)).value
    assets(webpackBuildDir).pair(relativeTo(webpackBuildDir))
  }
}
