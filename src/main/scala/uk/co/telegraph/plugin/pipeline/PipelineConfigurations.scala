package uk.co.telegraph.plugin.pipeline

import sbt.Configurations._
trait PipelineConfigurations {
  lazy val DeployStatic  = config("static" ) extend Runtime
  lazy val DeployDev     = config("dev"    ) extend Runtime
  lazy val DeployPreProd = config("preprod") extend Runtime
  lazy val DeployProd    = config("prod"   ) extend Runtime
}

object PipelineConfigurations extends PipelineConfigurations
