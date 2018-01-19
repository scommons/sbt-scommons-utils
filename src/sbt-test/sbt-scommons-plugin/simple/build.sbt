
lazy val root = (project in file("."))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.12.2",

    libraryDependencies ++= Seq(
      "org.scommons" %%% "scommons-client" % "0.1.0-SNAPSHOT"
    ),

    //our plugin settings
    scommonsResourcesFileFilter := "*.css",

    //scala.js specific settings
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalaJSUseMainModuleInitializer := true,
    webpackBundlingMode := BundlingMode.LibraryOnly(),
    emitSourceMaps := false
  )
