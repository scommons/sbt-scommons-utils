package scommons.sbtplugin.mecha

import com.typesafe.config.{ConfigFactory, ConfigObject}
import sbt.File

import scala.collection.JavaConverters._
import scala.collection.mutable

/*
 * NOTE: Temporarily put here, until mecha sbt-plugin published for sbt 1.x
 */
object ConfigParsers {

  /** Parse repository configuration from Hocon. */
  def reposFromHocon(file: File): Map[String, Repo] = {
    val repomap = mutable.Map[String, Repo]()
    val config = ConfigFactory.parseFile(file)
    for ((name, r: ConfigObject) <- config.root.asScala) {
      val repo = r.toConfig
      repomap(name) = Repo(
        dir = repo.getString("dir"),
        origin = repo.getString("origin"),
        mirrors = repo.getStringList("mirrors").asScala,
        ref = if (repo.hasPath("ref")) Some(repo.getString("ref")) else None
      )
    }
    repomap.toMap
  }
}
