package uk.co.telegraph.plugin.pipeline

import sbt.Project.inConfig
import sbt._

object PipelinePlugin extends AutoPlugin{

  object autoImport extends PipelineKeys with PipelineConfigurations

  import autoImport._


  lazy val staticDeploySettings : Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv := "static"
  )
  lazy val devDeploySettings    : Seq[Setting[_]] = baseStackSettings
  lazy val preprodDeploySettings: Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv := "preprod"
  )
  lazy val prodDeploySettings   : Seq[Setting[_]] = baseStackSettings ++ Seq(
    stackEnv := "prod"
  )

  override def trigger: PluginTrigger = allRequirements

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(autoImport.DeployStatic )(staticDeploySettings ) ++
    inConfig(autoImport.DeployProd   )(prodDeploySettings   ) ++
    inConfig(autoImport.DeployPreProd)(preprodDeploySettings) ++
    inConfig(autoImport.DeployDev    )(devDeploySettings    ) ++
    devDeploySettings
}
