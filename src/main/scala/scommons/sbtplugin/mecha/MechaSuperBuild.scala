package scommons.sbtplugin.mecha

import sbt._

/*
 * NOTE: Temporarily put here, until mecha sbt-plugin published for sbt 1.x
 */
trait MechaSuperBuild {

  lazy val superDirectory = file(".")

  /** File that describes all the repositories in this superrepository.
    *
    *  Format if extension is `json`:
    *
    *      {
    *        "super-project": {
    *          "dir": ".",
    *          "origin": "<repo-url-at-github>",
    *          "mirrors": ["<repo-url-at-bitbucket>"]
    *        },
    *        "sub-project": {
    *          "dir": "mecha",
    *          "origin": "git@github.com:storm-enroute/mecha.git",
    *          "mirrors": []
    *        }
    *      }
    *
    *  Format if extension is `conf`:
    *
    *      super-project {
    *        dir = "."
    *        origin = "<repo-url-at-github"
    *        mirrors = ["<repo-url-at-bitbucket"]
    *      }
    *      sub-project {
    *        dir = "mecha"
    *        origin = "git@github.com:storm-enroute/mecha.git"
    *        mirrors = []
    *      }
    *
    *  Override this method to specify a different path to this file.
    */
  def repositoriesFile: File = file("repos.conf")

  /** Holds the configuration of repositories in this superrepo.
    */
  lazy val repositories: Map[String, Repo] = {
    ConfigParsers.reposFromHocon(repositoriesFile)
  }

  def projects: Seq[ProjectReference] = {
    for {
      (_, repo) <- repositories.toList
      dir = file(repo.dir)
      if dir.exists
      if superDirectory != dir
    } yield repo.ref match {
      case None => RootProject(uri(repo.dir))
      case Some(r) => ProjectRef(uri(repo.dir), r)
    }
  }
}
