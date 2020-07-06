package scommons.sbtplugin.util

import sbt._
import scommons.sbtplugin.util.BundlesUtils._

class BundlesUtilsSpec extends BaseUtilsSpec {

  it should "skip classpath entry if it doesn't exists" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file = new File(sourceDir, "test-dir")
    val logger = mockFunction[String, Unit]

    //when & then
    genFromClasspath(logger, targetDir, Attributed.blankSeq(List(file)), "*.sql")
  }

  it should "skip classpath entry if its not a directory" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    val file = new File(sourceDir, "test-artifact.jar")
    IO.write(file, "invalid source")
    val logger = mockFunction[String, Unit]

    //when & then
    genFromClasspath(logger, targetDir, Attributed.blankSeq(List(file)), "*.sql")
  }

  it should "generate bundle files only once" in {
    //given
    val sourceDir = tmpSourceDir.get
    val targetDir = tmpTargetDir.get
    writeFile(sourceDir, "com/t/test.png", "file 0")
    val (_, contents1) = writeFile(sourceDir, "com/t/test.sql", "file 1")
    val (_, contents2) = writeFile(sourceDir, "com/t/test2.sql", "file 2")
    val (_, contents3) = writeFile(sourceDir, "com/t3/test.sql", "file 3")
    val logger = mockFunction[String, Unit]
    inSequence {
      logger.expects(s"Generated 2 bundle files (out of 2)" +
        s"\n\t$targetDir/com/t/bundle.json" +
        s"\n\t$targetDir/com/t3/bundle.json")
      logger.expects(s"Nothing to generate, all 2 bundle files are up to date" +
        s"\n\t$targetDir/com/t/bundle.json" +
        s"\n\t$targetDir/com/t3/bundle.json")
    }

    //when
    genFromClasspath(logger, targetDir, Attributed.blankSeq(List(sourceDir)), "*.sql")
    genFromClasspath(logger, targetDir, Attributed.blankSeq(List(sourceDir)), "*.sql")

    //then
    assertFile(targetDir, "com/t/bundle.json",
      s"""[ {
         |  "file" : "test.sql",
         |  "content" : "$contents1"
         |}, {
         |  "file" : "test2.sql",
         |  "content" : "$contents2"
         |} ]""".stripMargin)
    
    assertFile(targetDir, "com/t3/bundle.json",
      s"""[ {
         |  "file" : "test.sql",
         |  "content" : "$contents3"
         |} ]""".stripMargin)
  }
}
