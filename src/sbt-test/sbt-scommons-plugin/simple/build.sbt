
import play.sbt._

lazy val root = (project in file("."))
  .aggregate(
    client,
    server
  )

lazy val client = (project in file("client"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin, ScalaJSWeb)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.8",

    libraryDependencies ++= Seq(
      ("com.googlecode.web-commons" % "web-common-client" % "1.0.5").intransitive()
    ),

    //our plugin settings
    scommonsResourcesFileFilter := "*.css" || "*.png",
    scommonsResourcesArtifacts ++= Seq(
      "com.googlecode.web-commons" % "web-common-client" % "*"
    ),

    //scala.js specific settings
    //scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true,
    //webpackBundlingMode := BundlingMode.LibraryOnly(),
    version in webpack := "1.14.0", //TODO: migrate to default (latest version)
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "test.webpack.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "test.webpack.config.js"),
    emitSourceMaps := false,

    npmDevDependencies in Compile ++= Seq(
      "css-loader" -> "0.23.1",
      "extract-text-webpack-plugin" -> "1.0.1",
      "resolve-url-loader" -> "2.0.2",
      "url-loader" -> "0.5.8",
      "file-loader" -> "0.11.2",
      "style-loader" -> "0.13.1",
      "webpack-merge" -> "4.1.0"
    )
  )

lazy val server = (project in file("server"))
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.8",

    libraryDependencies ++= Seq(
      "org.scaldi" %% "scaldi-play" % "0.5.17",
      PlayImport.guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.19" % Test
    ),

    scalaJSProjects := Seq(client),
    pipelineStages in Assets := Seq(scalaJSPipeline),

    // Expose as sbt-web assets some webpack build files of the `client` project
    webpackAssets in fastOptJS ++= WebpackAssets.ofScalaJSProjects(fastOptJS) { build => (build / "styles").allPaths }.value,
    webpackAssets in fullOptJS ++= WebpackAssets.ofScalaJSProjects(fullOptJS) { build => (build / "styles").allPaths }.value
  )
