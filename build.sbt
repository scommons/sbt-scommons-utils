
val ideExcludedDirectories = SettingKey[Seq[File]]("ide-excluded-directories")

lazy val `sbt-scommons-plugin` = (project in file("."))
  .enablePlugins(SbtPlugin)
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
    scalaVersion := "2.12.7",
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
    
    ideExcludedDirectories := {
      val base = baseDirectory.value
      List(
        base / ".idea",
        base / "target"
      )
    },
    
    //when run tests with coverage: "sbt clean coverage test coverageReport"
    coverageMinimum := 80,
    coverageHighlighting := false,
    coverageExcludedPackages := ".*mecha.*;.*project.*",

    addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0"),
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.31"),
    addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler-sjs06" % "0.16.0"),
    addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler-sjs06" % "0.16.0"),

    // when updating plugins versions here,
    // don't forget to set the same versions in `scommons.sbtplugin.project.CommonLibs` !!!
    //
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.21"), // same as CommonLibs.playVer
    addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2"),
    addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3"),
    
    //addSbtPlugin("com.storm-enroute" % "mecha" % "0.3"), //TODO: use version for sbt 1.x
    
    addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1"),
    addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.4"),

    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.5"),
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1"),

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.2" % "test",
      "org.scalamock" %% "scalamock" % "4.4.0" % "test"
    ),

    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",

    //
    // publish/release related settings:
    //
    sonatypeProfileName := "org.scommons",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := sonatypePublishToBundle.value,
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
