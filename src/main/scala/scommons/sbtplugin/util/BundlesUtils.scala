package scommons.sbtplugin.util

import play.api.libs.json.Json
import sbt._

object BundlesUtils {

  def genFromClasspath(logger: String => Unit,
                       targetDir: File,
                       cp: Seq[Attributed[File]],
                       fileFilter: FileFilter): Unit = {

    for (entry <- cp) {
      val cpEntry = entry.data
      if (cpEntry.exists && cpEntry.isDirectory) {
        var bundles = Map.empty[File, (Seq[File], Long)]
        for ((file, relPath) <- Path.selectSubpaths(cpEntry, fileFilter)) {
          val targetFile = new File(targetDir, relPath)
          val bundleDir = targetFile.getParentFile
          val (bundleFiles, lastModified) = bundles.getOrElse(bundleDir, (Nil, 0L))
          bundles = bundles.updated(bundleDir, (bundleFiles :+ file,
            if (lastModified < file.lastModified()) file.lastModified()
            else lastModified
          ))
        }
        
        var generated = Seq.empty[File]
        for ((bundleDir, (bundleFiles, lastModified)) <- bundles) {
          val bundle = new File(bundleDir, "bundle.json")
          if (!bundle.exists() || bundle.lastModified() < lastModified) {
            generated = generated :+ bundle
            
            val content = bundleFiles.map { file =>
              Json.obj(
                "file" -> file.getName,
                "content" -> IO.read(file)
              )
            }
            
            IO.write(bundle, Json.prettyPrint(Json.toJson(content)))
            bundle.setLastModified(lastModified)
          }
        }
        
        def print(xs: Seq[File]): String = {
          xs.map(_.toString).sorted.mkString("\n\t")
        }

        val total = bundles.size
        if (generated.nonEmpty) {
          logger(s"Generated ${generated.size} bundle files (out of $total)" +
            s"\n\t${print(generated)}")
        }
        else if (total > 0) {
          logger(s"Nothing to generate, all $total bundle files are up to date" +
            s"\n\t${print(bundles.keys.map(new File(_, "bundle.json")).toSeq)}")
        }
      }
    }
  }
}
