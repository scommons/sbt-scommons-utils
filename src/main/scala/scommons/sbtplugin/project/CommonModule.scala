package scommons.sbtplugin.project

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.mecha.MechaProjectBuild
import scoverage.ScoverageKeys._
import scoverage.ScoverageSbtPlugin

trait CommonModule extends ProjectDef with MechaProjectBuild {

  override def definition: Project = Project(id = id, base = base)
    .dependsOn(internalDependencies: _*)
    .settings(CommonModule.settings: _*)
    .settings(
      libraryDependencies ++= (runtimeDependencies.value ++ testDependencies.value)
    )
    .settings(Seq(
      libraryDependencies --= excludeSuperRepoDependencies.value
    ))
    .dependsOnSuperRepo

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Nil
}

object CommonModule {

  // got from here:
  // https://github.com/JetBrains/sbt-ide-settings
  //
  val ideExcludedDirectories = SettingKey[Seq[File]]("ide-excluded-directories")

  val settings: Seq[Setting[_]] = Seq(
    scalaVersion := "2.12.2",
    scalacOptions ++= Seq(
      //"-Xcheckinit",
      "-Xfatal-warnings",
      "-Xlint:_",
      "-explaintypes",
      "-unchecked",
      "-deprecation",
      "-feature"
    ),
    //ivyScala := ivyScala.value map {
    //  _.copy(overrideScalaVersion = true)
    //},
    ideExcludedDirectories := {
      val base = baseDirectory.value
      List(
        base / ".idea",
        base / "target"
      )
    },
    //when run tests with coverage: "sbt clean coverage test it:test coverageReport && sbt coverageAggregate"
    coverageMinimum := 80,

    //use patched versions by now, to make scoverage work with scalajs-bundler
    libraryDependencies ++= {
      if (coverageEnabled.value) {
        Seq(
          Def.setting("org.scommons.patched" %%% "scalac-scoverage-runtime" % "1.4.0-SNAPSHOT").value,
          "org.scommons.patched" %% "scalac-scoverage-plugin" % "1.4.0-SNAPSHOT" % ScoverageSbtPlugin.ScoveragePluginConfig.name
        )
      }
      else Nil
    },
    libraryDependencies ~= (_.map(_.exclude("org.scoverage", "scalac-scoverage-runtime_2.12"))),
    libraryDependencies ~= (_.map(_.exclude("org.scoverage", "scalac-scoverage-runtime_sjs0.6_2.12"))),
    libraryDependencies ~= (_.map(_.exclude("org.scoverage", "scalac-scoverage-plugin_2.12"))),

    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
  )
}
