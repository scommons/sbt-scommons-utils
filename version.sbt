version in ThisBuild := sys.env.getOrElse("version", default = "0.4.0-SNAPSHOT").stripPrefix("v")
