package scommons.sbtplugin

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import scommons.sbtplugin.util.{BundlesUtils, ResourcesUtils}

import scalajsbundler.Webpack
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport._

object ScommonsPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires = ScalaJSBundlerPlugin

  object autoImport {
    val scommonsResourcesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
      "File filter of resources files, that should be automatically copied/extracted to the webpack directory"
    )
    val scommonsResourcesArtifacts: SettingKey[Seq[ModuleID]] = settingKey[Seq[ModuleID]](
      "List of artifacts (JARs) with resources, that should be automatically extracted to the webpack directory"
    )
    
    val scommonsBundlesFileFilter: SettingKey[FileFilter] = settingKey[FileFilter](
      "File filter of bundles files, that should be automatically generated in the webpack directory"
    )
    
    val scommonsNodeJsTestLibs: SettingKey[Seq[String]] = settingKey[Seq[String]](
      "List of JavaScript files, that should be pre-pended to the test fastOptJS output, useful for module mocks"
    )

    val scommonsRequireWebpackInTest: SettingKey[Boolean] = settingKey[Boolean](
      "Whether webpack command should be executed during tests, use webpackConfigFile for custom configuration"
    )
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    
    scommonsResourcesFileFilter :=
      "*.js" ||
        "*.json" ||
        "*.css" ||
        "*.ico" ||
        "*.png" ||
        "*.jpg" ||
        "*.jpeg" ||
        "*.gif" ||
        "*.svg" ||
        "*.ttf" ||
        "*.mp3" ||
        "*.wav" ||
        "*.mp4" ||
        "*.mov" ||
        "*.html" ||
        "*.pdf",
    
    scommonsResourcesArtifacts := Seq(
      "org.scommons.react" % "scommons-react-core" % "*",
      "org.scommons.client" % "scommons-client-ui" % "*"
    ),

    scommonsBundlesFileFilter := NothingFilter,

    scommonsNodeJsTestLibs := Nil,

    scommonsRequireWebpackInTest := false,
    
    sjsStageSettings(fastOptJS, Compile),
    sjsStageSettings(fullOptJS, Compile),
    sjsStageSettings(fastOptJS, Test),
    sjsStageSettings(fullOptJS, Test),

    fastOptJS in Test := {
      val logger = streams.value.log
      val testLibs = scommonsNodeJsTestLibs.value
      val sjsOutput = (fastOptJS in Test).value
      val targetDir = sjsOutput.data.getParentFile
      val bundleOutput =
        if (testLibs.nonEmpty) {
          val sjsOutputName = sjsOutput.data.name.stripSuffix(".js")
          val bundle = new File(targetDir, s"$sjsOutputName-bundle.js")
  
          logger.info(s"Writing NodeJs test bundle\n\t$bundle")
          IO.delete(bundle)
          testLibs.foreach { jsFile =>
            IO.write(bundle, IO.read(new File(targetDir, jsFile)), append = true)
          }
          IO.write(bundle, IO.read(sjsOutput.data), append = true)
  
          Attributed(bundle)(sjsOutput.metadata)
        }
        else sjsOutput

      if (scommonsRequireWebpackInTest.value) {
        val customWebpackConfigFile = (webpackConfigFile in Test).value
        val nodeArgs = (webpackNodeArgs in Test).value
        val bundleName = bundleOutput.data.name.stripSuffix(".js")
        val webpackOutput = targetDir / s"$bundleName-webpack-out.js"

        logger.info("Executing webpack...")
        val loader = bundleOutput.data

        customWebpackConfigFile match {
          case Some(configFile) =>
            val customConfigFileCopy = Webpack.copyCustomWebpackConfigFiles(targetDir, webpackResources.value.get)(configFile)
            Webpack.run(nodeArgs: _*)("--mode", "development", "--config", customConfigFileCopy.getAbsolutePath, loader.absolutePath, "--output", webpackOutput.absolutePath)(targetDir, logger)
          case None =>
            Webpack.run(nodeArgs: _*)("--mode", "development", loader.absolutePath, "--output", webpackOutput.absolutePath)(targetDir, logger)
        }

        Attributed(webpackOutput)(bundleOutput.metadata)
      }
      else bundleOutput
    },

    // revert the change for clean task: https://github.com/sbt/sbt/pull/3834/files#r172686677
    // to keep the logic for cleanKeepFiles and avoid the error:
    //   cleanKeepFiles contains directory/file that are not directly under cleanFiles
    clean := doClean(Seq(managedDirectory.value, target.value), cleanKeepFiles.value),

    cleanKeepFiles ++= Seq(
      target.value / "scala-2.12" / "scalajs-bundler" / "main" / "node_modules",
      target.value / "scala-2.12" / "scalajs-bundler" / "test" / "node_modules",
      target.value / "scalajs-bundler-jsdom" / "node_modules"
    )
  )

  private def sjsStageSettings(sjsStage: TaskKey[Attributed[File]], config: ConfigKey) = {
    sjsStage in config := {
      copyWebpackResources(
        streams.value.log,
        (crossTarget in (config, sjsStage)).value,
        (fullClasspath in config).value,
        scommonsResourcesFileFilter.value,
        scommonsResourcesArtifacts.value
      )
      genWebpackBundles(
        streams.value.log,
        (crossTarget in (config, sjsStage)).value,
        (fullClasspath in config).value,
        scommonsBundlesFileFilter.value
      )
      (sjsStage in config).value
    }
  }

  private def copyWebpackResources(log: Logger,
                                   webpackDir: File,
                                   cp: Seq[Attributed[File]],
                                   fileFilter: FileFilter,
                                   includeArtifacts: Seq[ModuleID]): Unit = {

    ResourcesUtils.extractFromClasspath(msg => log.info(msg), webpackDir, cp, fileFilter, includeArtifacts)
  }

  private def genWebpackBundles(log: Logger,
                                webpackDir: File,
                                cp: Seq[Attributed[File]],
                                fileFilter: FileFilter): Unit = {

    if (fileFilter != NothingFilter) {
      BundlesUtils.genFromClasspath(msg => log.info(msg), webpackDir, cp, fileFilter)
    }
  }

  private def doClean(clean: Seq[File], preserve: Seq[File]): Unit =
    IO.withTemporaryDirectory { temp =>
      val (dirs, files) = preserve.filter(_.exists).flatMap(_.allPaths.get).partition(_.isDirectory)
      val mappings = files.zipWithIndex map { case (f, i) => (f, new File(temp, i.toHexString)) }
      IO.move(mappings)
      IO.delete(clean)
      IO.createDirectories(dirs) // recreate empty directories
      IO.move(mappings.map(_.swap))
    }
}
