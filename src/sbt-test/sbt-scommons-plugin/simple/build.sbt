
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
    scommonsBundlesFileFilter := "*.sql",

    //scala.js specific settings
    //scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true,
    //webpackBundlingMode := BundlingMode.LibraryOnly(),
    version in webpack := "4.29.0",
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "client.webpack.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "client.webpack.config.js"),
    scommonsRequireWebpackInTest := true,
    webpackConfigFile in Test := Some(baseDirectory.value / "test.webpack.config.js"),
    
    emitSourceMaps := false,
    webpackEmitSourceMaps := false,

    npmDevDependencies in Compile ++= Seq(
      "css-loader" -> "2.1.1",
      "mini-css-extract-plugin" -> "0.12.0",
      "resolve-url-loader" -> "3.1.2",
      "url-loader" -> "4.1.1",
      "webpack-node-externals" -> "2.5.2",
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
    pipelineStages in Assets := Seq(scalaJSPipeline)
  )
