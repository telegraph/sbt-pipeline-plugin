{
  Option(System.getProperty("plugin.version")) match {
    case Some(pluginVersion) =>
      addSbtPlugin("uk.co.telegraph" % "sbt-pipeline-plugin" % pluginVersion)
    case _ =>
      throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                    |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  }
}