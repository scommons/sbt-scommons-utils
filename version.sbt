version in ThisBuild := sys.env.getOrElse("version", default = "0.5.0-SNAPSHOT").stripPrefix("v")
