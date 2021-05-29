package scommons.sbtplugin.util

import java.io._
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.scalamock.scalatest.MockFactory
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sbt._

abstract class BaseUtilsSpec extends AnyFlatSpec
  with Matchers
  with BeforeAndAfterEach
  with MockFactory {

  protected var tmpSourceDir: Option[File] = None
  protected var tmpTargetDir: Option[File] = None

  override protected def beforeEach(): Unit = {
    tmpSourceDir = Some(createTmpDir(tmpSourceDir, "scommons.sbtplugin.sourceDir."))
    tmpTargetDir = Some(createTmpDir(tmpTargetDir, "scommons.sbtplugin.targetDir."))
  }

  override protected def afterEach(): Unit = {
    tmpSourceDir = {
      deleteDirRecursively(tmpSourceDir.get)
      tmpSourceDir.get.exists() shouldBe false
      None
    }
    tmpTargetDir = {
      deleteDirRecursively(tmpTargetDir.get)
      tmpTargetDir.get.exists() shouldBe false
      None
    }
  }

  def assertFile(dir: File, relPathName: String, contents: String, exists: Boolean = true): Assertion = {
    val file = new File(s"${dir.getPath}${File.separator}$relPathName")
    file.exists() shouldBe exists

    if (exists) {
      IO.read(file) shouldBe contents
    }

    succeed
  }

  def writeFile(dir: File, relPathName: String, contents: String): (String, String) = {
    val file = new File(s"${dir.getPath}/$relPathName")
    IO.write(file, contents)
    (relPathName, contents)
  }

  def writeZipFile(file: File, relPathNamesWithContents: List[(String, String)]): List[(String, String)] = {
    val stream = new ZipOutputStream(new FileOutputStream(file))
    try {
      for ((relPathName, contents) <- relPathNamesWithContents) {
        stream.putNextEntry(new ZipEntry(relPathName))
        stream.write(contents.getBytes("UTF-8"))
        stream.closeEntry()
      }

      stream.finish()
      relPathNamesWithContents
    } finally {
      stream.close()
    }
  }

  private def createTmpDir(currTmpDir: Option[File], prefix: String): File = {
    if (currTmpDir.isEmpty) {
      val tmpFile = File.createTempFile(prefix, "")
      deleteFile(tmpFile)
      if (!tmpFile.mkdirs()) {
        throw new IllegalStateException(s"Cannot create directory(s): $tmpFile")
      }

      tmpFile
    }
    else {
      throw new IllegalStateException("Temp directory already exists, probably it was not cleaned-up properly")
    }
  }

  private def deleteFile(file: File): Unit = {
    if (!file.delete()) {
      throw new IllegalStateException(s"Cannot delete file/directory: $file")
    }
  }

  private def deleteDirRecursively(dir: File): Unit = {
    val filter = new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = name != "." && name != ".."
    }

    val (dirs, files) = dir.listFiles(filter).partition(_.isDirectory)

    for (dir <- dirs) {
      deleteDirRecursively(dir)
    }

    for (file <- files) {
      deleteFile(file)
    }

    deleteFile(dir)
  }
}
