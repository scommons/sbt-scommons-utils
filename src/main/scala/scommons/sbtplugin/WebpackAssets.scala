package scommons.sbtplugin

import com.typesafe.sbt.web.PathMapping
import sbt.Def._
import sbt._
import webscalajs.WebScalaJS.autoImport._

import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object WebpackAssets {

  def ofProject(sjsStage: TaskKey[Attributed[File]], project: ProjectReference)
               (assets: File => PathFinder): Def.Initialize[Task[Seq[PathMapping]]] = Def.task {

    // resolve webpack assets after webpack task is finished
    val _ = (project / Compile / sjsStage / webpack).value

    val webpackBuildDir = (project / Compile / npmUpdate).value
    assets(webpackBuildDir).pair(Path.relativeTo(webpackBuildDir))
  }

  def ofScalaJSProjects(sjsStage: TaskKey[Attributed[File]])
                       (assets: File => PathFinder): Def.Initialize[Task[Seq[PathMapping]]] = Def.taskDyn {

    val projects = scalaJSProjects.value.map(Project.projectToRef)
    
    Def.taskDyn {
      // resolve webpack assets after webpack task is finished
      val _ = taskOnProjectsOnTask(projects, sjsStage, webpack).value

      val webpackBuildDirs = taskOnProjects(projects, npmUpdate).value
      
      Def.task {
        webpackBuildDirs.flatMap { webpackBuildDir =>
          assets(webpackBuildDir).pair(Path.relativeTo(webpackBuildDir))
        }
      }
    }
  }

  private def taskOnProjectsOnTask[A](projects: Seq[ProjectReference],
                                      sjsStage: TaskKey[Attributed[File]],
                                      action: Initialize[Task[Seq[A]]]): Initialize[Task[Seq[A]]] = Def.taskDyn {

    val scopeFilter = ScopeFilter(inProjects(projects: _*), inConfigurations(Compile), inTasks(sjsStage))
    Def.task {
      action.all(scopeFilter).value.flatten
    }
  }

  private def taskOnProjects[A](projects: Seq[ProjectReference],
                                action: Initialize[Task[A]]): Initialize[Task[Seq[A]]] = Def.taskDyn {

    val scopeFilter = ScopeFilter(inProjects(projects: _*), inConfigurations(Compile))
    Def.task {
      action.all(scopeFilter).value
    }
  }
}
