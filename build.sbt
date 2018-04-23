
lazy val `sbt-scommons-plugin` = (project in file("."))
  .settings(ScriptedPlugin.scriptedSettings)
  .settings(
    scriptedLaunchOpts := { scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )
  .settings(
    sbtPlugin := true,
    organization := "org.scommons.sbt",
    name := "sbt-scommons-plugin",
    description := "Sbt auto-plugin with common tasks/utils for Scala Commons modules",
    scalaVersion := "2.10.6",
    scalacOptions ++= Seq(
      //"-Xcheckinit",
      "-Xfatal-warnings",
      "-feature",
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    ),
    //ivyScala := ivyScala.value map {
    //  _.copy(overrideScalaVersion = true)
    //},
    ideaExcludeFolders := {
      val base = baseDirectory.value
      List(
        s"$base/.idea",
        s"$base/target"
      )
    },
    //when run tests with coverage: "sbt clean coverage test coverageReport"
    coverageMinimum := 80,
    coverageHighlighting := false,
    coverageExcludedPackages := ".*mecha.*",

    addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.9.0"),
    addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.9.0"),
    addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.6"),
    addSbtPlugin("com.storm-enroute" % "mecha" % "0.3"),

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
    ),

    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",

    //
    // publish/release related settings:
    //
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := {
      if (isSnapshot.value)
        Some("snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
      else
        Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/scommons/sbt-scommons-plugin</url>
        <licenses>
          <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0</url>
            <distribution>repo</distribution>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:scommons/sbt-scommons-plugin.git</url>
          <connection>scm:git@github.com:scommons/sbt-scommons-plugin.git</connection>
        </scm>
        <developers>
          <developer>
            <id>viktorp</id>
            <name>Viktor Podzigun</name>
            <url>https://github.com/viktor-podzigun</url>
          </developer>
        </developers>
    },
    pomIncludeRepository := {
      _ => false
    }
  )
