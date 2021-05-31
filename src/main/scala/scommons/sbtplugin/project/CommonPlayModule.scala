package scommons.sbtplugin.project

import com.typesafe.sbt.digest.Import.digest
import com.typesafe.sbt.gzip.Import.gzip
import com.typesafe.sbt.web.SbtWeb.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play.sbt.routes.RoutesKeys
import play.sbt.{PlayImport, PlayLayoutPlugin, PlayScala}
import sbt._
import scommons.sbtplugin.WebpackAssetsPlugin.autoImport._
import scoverage.ScoverageKeys.coverageExcludedPackages
import webscalajs.WebScalaJS.autoImport._

import scalajsbundler.sbtplugin.WebScalaJSBundlerPlugin

trait CommonPlayModule extends CommonModule {

  def scommonsClientVersion: String

  def scommonsServiceVersion: String
  
  def scommonsApiVersion: String
  
  override def definition: Project = {
    super.definition
      .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
      .disablePlugins(PlayLayoutPlugin)
      .configs(IntegrationTest)
      .settings(Defaults.itSettings: _*)
      .settings(
        RoutesKeys.routesImport -= "controllers.Assets.Asset", //remove unused import warning from routes file
        coverageExcludedPackages := "<empty>;Reverse.*;router.*",

        pipelineStages in Assets := Seq(scalaJSPipeline),
        pipelineStages := Seq(digest, gzip),

        devCommands in scalaJSPipeline ++= Seq("test", "testOnly"),

        // Expose as sbt-web assets some webpack build files of the scalajs projects
        //
        webpackAssets in fastOptJS ++= WebpackAssets.ofScalaJSProjects(fastOptJS) { build => (build / "styles").allPaths }.value,
        webpackAssets in fullOptJS ++= WebpackAssets.ofScalaJSProjects(fullOptJS) { build => (build / "styles").allPaths }.value
      )
  }

  override def internalDependencies: Seq[ClasspathDep[ProjectReference]] = Nil

  override def superRepoProjectsDependencies: Seq[(String, String, Option[String])] = Seq(
    ("scommons-service", "scommons-service-util", None),
    ("scommons-service", "scommons-service-play", None),
    ("scommons-client", "scommons-client-assets", None),
    // tests
    ("scommons-service", "scommons-service-test", Some("it,test"))
  )

  override def runtimeDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.service" %% "scommons-service-util" % scommonsServiceVersion,
    "org.scommons.service" %% "scommons-service-play" % scommonsServiceVersion,
    "org.scommons.client" %% "scommons-client-assets" % scommonsClientVersion,

    PlayImport.guice,
    CommonLibs.play.value,
    CommonLibs.scaldiPlay.value,
    
    CommonLibs.logback.value,
    CommonLibs.slf4jApi.value
  ))

  override def testDependencies: Def.Initialize[Seq[ModuleID]] = Def.setting(Seq(
    "org.scommons.service" %% "scommons-service-test" % scommonsServiceVersion,
    "org.scommons.api" %% "scommons-api-play-ws" % scommonsApiVersion,
    
    CommonTestLibs.scalaTestPlusPlay.value,
    CommonTestLibs.akkaStreamTestKit.value,
    CommonTestLibs.scalaTestPlusMockito.value
  ).map(_ % "it,test"))
}
