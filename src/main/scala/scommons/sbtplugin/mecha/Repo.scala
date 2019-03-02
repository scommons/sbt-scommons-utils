package scommons.sbtplugin.mecha

/*
 * NOTE: Temporarily put here, until mecha sbt-plugin published for sbt 1.x
 */
case class Repo(dir: String, origin: String, mirrors: Seq[String],
                ref: Option[String] = None)
