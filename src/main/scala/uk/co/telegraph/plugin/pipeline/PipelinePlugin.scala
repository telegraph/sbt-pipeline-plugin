package uk.co.telegraph.plugin.pipeline

import sbt._
import uk.co.telegraph.cloud.AuthProfile

object PipelinePlugin extends AutoPlugin{

  object autoImport extends PipelineKeys with PipelineConfigurations {
    lazy val pipelineSettings: Seq[Setting[_]] =
      inConfig(DeployStatic)(baseStackSettings ++ Seq(
        stackEnv  := "static",
        stackAuth := AuthProfile(Some("prod"))
      )) ++
      inConfig(DeployDev)(baseStackSettings ++ Seq(
        stackEnv  := "dev",
        stackAuth := AuthProfile(Some("dev"))
      )) ++
      inConfig(DeployCt)(baseStackSettings ++ Seq(
        stackEnv  := "ct",
        stackAuth := AuthProfile(Some("preprod"))
      )) ++
      inConfig(DeployPreProd)(baseStackSettings ++ Seq(
        stackEnv  := "preprod",
        stackAuth := AuthProfile(Some("preprod"))
      )) ++
      inConfig(DeployProd)(baseStackSettings ++ Seq(
        stackEnv  := "prod",
        stackAuth := AuthProfile(Some("prod"))
      ))
  }

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings:Seq[Setting[_]] = pipelineSettings
}
