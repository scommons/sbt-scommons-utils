package scommons.sbtplugin.util

import java.io.{File, FilenameFilter}

import org.scalatest.{Assertion, BeforeAndAfterEach, FlatSpec, Matchers}
import sbt._

class ResourcesHelperSpec extends FlatSpec
  with Matchers
  with BeforeAndAfterEach {
  //with MockFactory {

  private var tmpSourceDir: Option[File] = None
  private var tmpTargetDir: Option[File] = None

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

  it should "copy local resource files" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val (pathName1, contents1) = writeFile(sourceDir, "com/t/test.png", "file 1")
    val (pathName2, contents2) = writeFile(sourceDir, "com/t/test.css", "file 2")
    val (pathName3, contents3) = writeFile(sourceDir, "com/t3/test.css", "file 3")

    //when
    ResourcesHelper.extractFromClasspath(targetDir, Attributed.blankSeq(List(sourceDir)), "*.css")

    //then
    assertFile(targetDir, pathName1, contents1, exists = false)
    assertFile(targetDir, pathName2, contents2)
    assertFile(targetDir, pathName3, contents3)
  }

  private def assertFile(dir: File, relPathName: String, contents: String, exists: Boolean = true): Assertion = {
    val file = new File(s"${dir.getPath}${File.separator}$relPathName")
    file.exists() shouldBe exists

    if (exists) {
      IO.read(file) shouldBe contents
    }

    succeed
  }

  private def writeFile(dir: File, relPathName: String, contents: String): (String, String) = {
    val file = new File(s"${dir.getPath}${File.separator}$relPathName")
    IO.write(file, contents)
    (relPathName, contents)
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
