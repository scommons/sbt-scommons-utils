package scommons.sbtplugin.util

import java.io.{FileInputStream, FilterInputStream, InputStream}
import java.util.zip.ZipInputStream

import sbt.Keys._
import sbt._

object ResourcesUtils {

  def extractFromClasspath(targetDir: File,
                           cp: Seq[Attributed[File]],
                           fileFilter: FileFilter,
                           includeArtifacts: Seq[ModuleID]): Unit = {

    for (entry <- cp) {
      val cpEntry = entry.data
      if (cpEntry.exists) {
        val cpName = cpEntry.getName
        if (cpEntry.isFile && (cpName.endsWith(".jar") || cpName.endsWith(".zip"))) {
          entry.get(moduleID.key) match {
            case Some(module) if includeArtifacts.isEmpty || includeArtifacts.exists { m =>
              m.organization == module.organization &&
                m.name == module.name
            } =>
              processEntries(new FileInputStream(cpEntry),
                pathName => fileFilter.accept(new File(pathName)),
                processEntry = { (relPath, stream) =>
                  val targetFile = new File(targetDir, relPath)
                  if (cpEntry.lastModified() != targetFile.lastModified()) {
                    println(s"Extracting file $targetFile")

                    IO.write(targetFile, IO.readBytes(stream))
                    targetFile.setLastModified(cpEntry.lastModified())
                    ()
                  }
                }
              )
            case _ => //skip
          }
        } else if (cpEntry.isDirectory) {
          for ((file, relPath) <- Path.selectSubpaths(cpEntry, fileFilter)) {
            val targetFile = new File(targetDir, relPath)
            if (file.lastModified() != targetFile.lastModified()) {
              println(s"Copying file $targetFile")

              IO.copyFile(file, targetFile, preserveLastModified = true)
            }
          }
        } else {
          throw new IllegalArgumentException(
            s"Illegal classpath entry, require directory, .jar or .zip file, but got: ${cpEntry.getPath}"
          )
        }
      }
    }
  }

  private def processEntries(inputStream: InputStream,
                             p: String => Boolean,
                             processEntry: (String, InputStream) => Unit): Unit = {

    val stream = new ZipInputStream(inputStream)
    try {
      val streamIgnoreClose = new FilterInputStream(stream) {
        override def close(): Unit = ()
      }

      Iterator.continually(stream.getNextEntry)
        .takeWhile(_ != null)
        .filter(entry => p(entry.getName))
        .foreach(entry => processEntry(entry.getName, streamIgnoreClose))
    } finally {
      stream.close()
    }
  }
}
