
[![Build Status](https://travis-ci.org/scommons/sbt-scommons-plugin.svg?branch=master)](https://travis-ci.org/scommons/sbt-scommons-plugin)
[![Coverage Status](https://coveralls.io/repos/github/scommons/sbt-scommons-plugin/badge.svg?branch=master)](https://coveralls.io/github/scommons/sbt-scommons-plugin?branch=master)
[![scala-index](https://index.scala-lang.org/scommons/sbt-scommons-plugin/sbt-scommons-plugin/latest.svg)](https://index.scala-lang.org/scommons/sbt-scommons-plugin/sbt-scommons-plugin)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.17.svg)](https://www.scala-js.org)

## sbt-scommons-plugin
Sbt auto-plugins with common tasks/utils for Scala Commons modules

### How to add it to your project

```scala
// inside plugins.sbt

addSbtPlugin("org.scommons.sbt" % "sbt-scommons-plugin" % "1.0.0-SNAPSHOT")
```

Latest `SNAPSHOT` version is published to [Sonatype Repo](https://oss.sonatype.org/content/repositories/snapshots/org/scommons/), just make sure you added
the proper dependency resolver to your `build.sbt` settings:
```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
```

### How to Build

To build and run all the tests use the following command:
```bash
sbt clean test publishM2 scripted
```

## Documentation

You can find documentation [here](https://scommons.org/sbt-scommons-plugin)
