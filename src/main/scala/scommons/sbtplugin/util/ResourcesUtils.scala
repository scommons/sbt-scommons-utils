package scommons.sbtplugin.util

import java.io.{FileInputStream, FilterInputStream, InputStream}
import java.util.zip.ZipInputStream

import sbt.Keys._
import sbt._

object ResourcesUtils {

  def extractFromClasspath(logger: String => Unit,
                           targetDir: File,
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
              module.organization == m.organization &&
                module.name.startsWith(m.name)
            } =>
              var total = 0
              var extracted = 0
              processEntries(new FileInputStream(cpEntry),
                pathName => fileFilter.accept(new File(pathName)),
                processEntry = { (relPath, stream) =>
                  val targetFile = new File(targetDir, relPath)
                  if (cpEntry.lastModified() != targetFile.lastModified()) {
                    IO.write(targetFile, IO.readBytes(stream))
                    targetFile.setLastModified(cpEntry.lastModified())

                    extracted += 1
                  }

                  total += 1
                }
              )

              if (extracted > 0) {
                logger(s"Extracted $extracted files (out of $total)" +
                  s"\n\tfrom: $cpEntry" +
                  s"\n\tto:   $targetDir")
              }
              else if (total > 0) {
                logger(s"Nothing to extract, all $total files are up to date" +
                  s"\n\tfrom: $cpEntry" +
                  s"\n\tto:   $targetDir")
              }
            case _ => //skip
          }
        } else if (cpEntry.isDirectory) {
          var total = 0
          var copied = 0
          for ((file, relPath) <- Path.selectSubpaths(cpEntry, fileFilter)) {
            val targetFile = new File(targetDir, relPath)
            if (file.lastModified() != targetFile.lastModified()) {
              IO.copyFile(file, targetFile, preserveLastModified = true)
              copied += 1
            }

            total += 1
          }

          if (copied > 0) {
            logger(s"Copied $copied files (out of $total)" +
              s"\n\tfrom: $cpEntry" +
              s"\n\tto:   $targetDir")
          }
          else if (total > 0) {
            logger(s"Nothing to copy, all $total files are up to date" +
              s"\n\tfrom: $cpEntry" +
              s"\n\tto:   $targetDir")
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
      val streamIgnoreClose = new InputStreamIgnoreClose(stream)

      Iterator.continually(stream.getNextEntry)
        .takeWhile(_ != null)
        .filter(entry => p(entry.getName))
        .foreach(entry => processEntry(entry.getName, streamIgnoreClose))
    } finally {
      stream.close()
    }
  }

  private[util] class InputStreamIgnoreClose(in: InputStream) extends FilterInputStream(in) {

    override def close(): Unit = ()
  }
}
