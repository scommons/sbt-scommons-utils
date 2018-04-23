
package scommons.sbtplugin.test

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {

  @JSExportTopLevel("scommons.sbtplugin.test.main")
  def main(args: Array[String]): Unit = {
    println(s"test: ${MainCss.test}, test_btn: ${MainCss.test_btn}")
  }
}
