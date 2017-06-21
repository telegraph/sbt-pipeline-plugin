package uk.co.telegraph.plugin.pipeline

import sbt.Project.inConfig
import sbt._
import uk.co.telegraph.cloud.AuthProfile

object PipelinePlugin extends AutoPlugin{

  object autoImport extends PipelineKeys with PipelineConfigurations {
    lazy val pipelineSettings = baseStackSettings ++
      inConfig(autoImport.DeployStatic )(staticDeploySettings ) ++
      inConfig(autoImport.DeployProd   )(prodDeploySettings   ) ++
      inConfig(autoImport.DeployPreProd)(preprodDeploySettings) ++
      inConfig(autoImport.DeployDev    )(devDeploySettings    )
  }

  import autoImport._

  lazy val staticDeploySettings : Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv := "static",
    stackAuth := AuthProfile(Some("prod"))
  )
  lazy val devDeploySettings    : Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv  := "dev",
    stackAuth := AuthProfile(Some("dev"))
  )
  lazy val preprodDeploySettings: Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv  := "preprod",
    stackAuth := AuthProfile(Some("preprod"))
  )
  lazy val prodDeploySettings   : Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv := "prod",
    stackAuth := AuthProfile(Some("prod"))
  )

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings = pipelineSettings
}
