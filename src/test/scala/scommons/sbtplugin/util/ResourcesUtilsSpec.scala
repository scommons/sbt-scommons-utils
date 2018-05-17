package scommons.sbtplugin.util

import java.io._
import java.util.zip.{ZipEntry, ZipOutputStream}

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Assertion, BeforeAndAfterEach, FlatSpec, Matchers}
import sbt.Keys._
import sbt._
import scommons.sbtplugin.util.ResourcesUtils._

class ResourcesUtilsSpec extends FlatSpec
  with Matchers
  with BeforeAndAfterEach
  with MockFactory {

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

  it should "fail if classpath entry is not a directory, not jar and not zip" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file = new File(sourceDir, "test-artifact.tar.gz")
    IO.write(file, "invalid source")
    val logger = mockFunction[String, Unit]

    //when
    val e = the[IllegalArgumentException] thrownBy {
      extractFromClasspath(logger, targetDir, Attributed.blankSeq(List(file)), "*.css", Nil)
    }

    //then
    e.getMessage shouldBe s"Illegal classpath entry, require directory, .jar or .zip file, but got: ${file.getPath}"
  }

  it should "skip classpath entry if it doesn't exists" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file = new File(sourceDir, "test-artifact.jar")
    val logger = mockFunction[String, Unit]

    //when & then
    extractFromClasspath(logger, targetDir, Attributed.blankSeq(List(file)), "*.css", Nil)
  }

  it should "copy local resource files only once" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val (pathName1, contents1) = writeFile(sourceDir, "com/t/test.png", "file 1")
    val (pathName2, contents2) = writeFile(sourceDir, "com/t/test.css", "file 2")
    val (pathName3, contents3) = writeFile(sourceDir, "com/t3/test.css", "file 3")
    val logger = mockFunction[String, Unit]
    inSequence {
      logger.expects(s"Copied 2 files (out of 2)" +
        s"\n\tfrom: $sourceDir" +
        s"\n\tto:   $targetDir")
      logger.expects(s"Nothing to copy, all 2 files are up to date" +
        s"\n\tfrom: $sourceDir" +
        s"\n\tto:   $targetDir")
    }

    //when
    extractFromClasspath(logger, targetDir, Attributed.blankSeq(List(sourceDir)), "*.css", Nil)
    extractFromClasspath(logger, targetDir, Attributed.blankSeq(List(sourceDir)), "*.css", Nil)

    //then
    assertFile(targetDir, pathName1, contents1, exists = false)
    assertFile(targetDir, pathName2, contents2)
    assertFile(targetDir, pathName3, contents3)
  }

  it should "extract resource files from zip-artifact" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file = new File(sourceDir, "test-artifact.zip")
    val relPathNamesWithContents = writeZipFile(file, List(
      "com/zip/test.png" -> "file 1",
      "com/zip/test.css" -> "file 2",
      "com/zip3/test.css" -> "file 3"
    ))
    val logger = mockFunction[String, Unit]
    logger.expects(s"Extracted 2 files (out of 2)" +
      s"\n\tfrom: $file" +
      s"\n\tto:   $targetDir")

    //when
    extractFromClasspath(logger, targetDir, List(
      Attributed(file)(AttributeMap(
        AttributeEntry(moduleID.key, ModuleID("com.org", file.getName, "*"))
      ))
    ), "*.css", Nil)

    //then
    for ((pathName, contents) <- relPathNamesWithContents) {
      if (pathName.endsWith(".png"))
        assertFile(targetDir, pathName, contents, exists = false)
      else
        assertFile(targetDir, pathName, contents)
    }
  }

  it should "extract resource files from jar-artifact only once" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file = new File(sourceDir, "test-artifact.jar")
    val relPathNamesWithContents = writeZipFile(file, List(
      "com/jar/test.png" -> "file 1",
      "com/jar/test.css" -> "file 2",
      "com/jar3/test.css" -> "file 3"
    ))
    val logger = mockFunction[String, Unit]
    inSequence {
      logger.expects(s"Extracted 2 files (out of 2)" +
        s"\n\tfrom: $file" +
        s"\n\tto:   $targetDir")
      logger.expects(s"Nothing to extract, all 2 files are up to date" +
        s"\n\tfrom: $file" +
        s"\n\tto:   $targetDir")
    }

    //when
    extractFromClasspath(logger, targetDir, List(
      Attributed(file)(AttributeMap(
        AttributeEntry(moduleID.key, ModuleID("com.org", file.getName, "*"))
      ))
    ), "*.css", Nil)
    extractFromClasspath(logger, targetDir, List(
      Attributed(file)(AttributeMap(
        AttributeEntry(moduleID.key, ModuleID("com.org", file.getName, "*"))
      ))
    ), "*.css", Nil)

    //then
    for ((pathName, contents) <- relPathNamesWithContents) {
      if (pathName.endsWith(".png"))
        assertFile(targetDir, pathName, contents, exists = false)
      else
        assertFile(targetDir, pathName, contents)
    }
  }

  it should "extract resource files from jar-artifact and filter artifacts" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file0 = new File(sourceDir, "test-artifact0.jar")
    val relPathNamesWithContents0 = writeZipFile(file0, List(
      "com/jar0/test.png" -> "file 1",
      "com/jar0/test.css" -> "file 2",
      "com/jar03/test.css" -> "file 3"
    ))
    val file = new File(sourceDir, "test-ModuleID.jar")
    val relPathNamesWithContents = writeZipFile(file, List(
      "com/ModuleID/test.png" -> "file 1",
      "com/ModuleID/test.css" -> "file 2",
      "com/ModuleID3/test.css" -> "file 3"
    ))
    val logger = mockFunction[String, Unit]
    logger.expects(s"Extracted 2 files (out of 2)" +
      s"\n\tfrom: $file" +
      s"\n\tto:   $targetDir")

    //when
    extractFromClasspath(logger, targetDir, List(
      Attributed(file0)(AttributeMap(
        AttributeEntry(moduleID.key, ModuleID("com.org0", file0.getName, "*"))
      )),
      Attributed(file)(AttributeMap(
        AttributeEntry(moduleID.key, ModuleID("com.org", file.getName, "*"))
      ))
    ), "*.css", List(
      ModuleID("com.org", file.getName, "*")
    ))

    //then
    for ((pathName, contents) <- relPathNamesWithContents0) {
      assertFile(targetDir, pathName, contents, exists = false)
    }

    for ((pathName, contents) <- relPathNamesWithContents) {
      if (pathName.endsWith(".png"))
        assertFile(targetDir, pathName, contents, exists = false)
      else
        assertFile(targetDir, pathName, contents)
    }
  }

  it should "not close input stream in InputStreamIgnoreClose class" in {
    //given
    val stream = mock[InputStreamIgnoreClose]
    val streamIgnoreClose = new InputStreamIgnoreClose(stream)

    //then
    (stream.close _).expects().never()

    //when
    streamIgnoreClose.close()
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
    val file = new File(s"${dir.getPath}/$relPathName")
    IO.write(file, contents)
    (relPathName, contents)
  }

  private def writeZipFile(file: File, relPathNamesWithContents: List[(String, String)]): List[(String, String)] = {
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
