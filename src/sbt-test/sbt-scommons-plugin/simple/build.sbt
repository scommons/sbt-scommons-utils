
lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.2",

    libraryDependencies ++= Seq(
      ("com.googlecode.web-commons" % "web-common-client" % "1.0.5").intransitive()
    ),

    //our plugin settings
    scommonsResourcesFileFilter := "*.css",
    scommonsResourcesArtifacts ++= Seq(
      "com.googlecode.web-commons" % "web-common-client" % "*"
    ),

    //scala.js specific settings
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    emitSourceMaps := false
  )
