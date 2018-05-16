---
layout: default
---

Welcome to the **Scala Commons Sbt Plugin** documentation page!

## Overview

![Overview](drawio/overview.svg)
[Preview](https://www.draw.io/?chrome=0&lightbox=1&url=https%3A%2F%2Fraw.githubusercontent.com%2Fscommons%2Fsbt-scommons-plugin%2Fmaster%2Fdocs%2Fdrawio%2Foverview.svg%3Ft%3D0) | [Edit](https://www.draw.io/?title=overview.svg&url=https%3A%2F%2Fraw.githubusercontent.com%2Fscommons%2Fsbt-scommons-plugin%2Fmaster%2Fdocs%2Fdrawio%2Foverview.svg%3Ft%3D0)

## ScommonsPlugin

The main purpose of this sbt `auto-plugin` is to copy/extract resource files
(`css`, `png`, `jpeg`) from project dependencies to the `webpack` folder
during the `npmUpdate` task, so they can be used later by `webpack` task.

This is very similar to copying/extracting of `*.js` files,
which is already provided by the `ScalaJSBundlerPlugin`.

Since this plugin is sbt `auto-plugin`, it will be automatically activated
for all the projects that depend on `ScalaJSBundlerPlugin`.

It provides the following settings:
```scala
val scommonsResourcesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
  "File filter of resources files, that should be automatically copied/extracted to the webpack directory"
)

val scommonsResourcesArtifacts: SettingKey[Seq[ModuleID]] = settingKey[Seq[ModuleID]](
  "List of artifacts (JARs) with resources, that should be automatically extracted to the webpack directory"
)
```

With default values:
```scala
scommonsResourcesFileFilter :=
  "*.css" ||
    "*.ico" ||
    "*.png" ||
    "*.jpg" ||
    "*.jpeg" ||
    "*.gif"

scommonsResourcesArtifacts := Seq(
  "org.scommons.client" % "scommons-client-ui" % "*"
)
```

You can extend/override the default values:
```scala
settings(
    scommonsResourcesFileFilter :=
        scommonsResourcesFileFilter.value || "*.svg",

    scommonsResourcesArtifacts ++= Seq(
      "your.org" % "your-dependency" % "*"
    )
)
```

## WebpackAssetsPlugin

The main purpose of this sbt `auto-plugin` is to make the assets (`css`),
produced by the `webpack` task, available as `sbt-web` assets.

This is very similar to the `npmAssets` task, which is already provided
by the `WebScalaJSBundlerPlugin`.

Since this plugin is sbt `auto-plugin`, it will be automatically activated
for all the projects that depend on `WebScalaJSBundlerPlugin`.

It provides the following settings:
```scala
val webpackAssets: TaskKey[Seq[PathMapping]] = taskKey[Seq[PathMapping]](
  "Assets (resources that are not CommonJS modules) produced by Webpack"
)
```

To use it, add the following to your web-project (server) settings:
```scala
settings(
    webpackAssets in fastOptJS ++= WebpackAssets.ofProject(fastOptJS, clientProject) { build => (build / "styles").*** }.value,
    webpackAssets in fullOptJS ++= WebpackAssets.ofProject(fullOptJS, clientProject) { build => (build / "styles").*** }.value
)
```

Here, `clientProject` is your client sbt `Project` definition.