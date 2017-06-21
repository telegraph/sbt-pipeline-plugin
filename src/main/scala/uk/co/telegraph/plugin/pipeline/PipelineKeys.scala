package uk.co.telegraph.plugin.pipeline

import com.amazonaws.services.cloudformation.model.Stack
import sbt.Keys._
import sbt._
import uk.co.telegraph.cloud.AuthProfile
import uk.co.telegraph.plugin.pipeline.tasks._

trait PipelineKeys {

  //Stack Keys
  lazy val stackEnv: SettingKey[String]               = SettingKey[String]     ("stack-env",            "Sets the infrastructure environment.")
  lazy val stackParamsPath: SettingKey[File]          = SettingKey[File]       ("stack-path-params",    "Sets the infrastructure Static Path.")
  lazy val stackTemplatePath: SettingKey[File]        = SettingKey[File]       ("stack-path-template",  "Sets the infrastructure Template Path.")
  lazy val stackSkip: SettingKey[Boolean]             = SettingKey[Boolean    ]("stack-static-skip",    "Skips the infrastructure deployment.")
  lazy val stackCustomParams: SettingKey[StackParams] = SettingKey[StackParams]("stack-params",         "Sets Custom Infrastructure Parameters")
  lazy val stackTags: SettingKey[StackTags]           = SettingKey[StackTags  ]("stack-tags",           "Tags of this stack")
  lazy val stackCapabilities: SettingKey[Seq[String]] = SettingKey[Seq[String]]("stack-capabilities",   "The list of capabilities that you want to allow in the stack . E.g.[CAPABILITY_IAM]")
  lazy val stackRegion: SettingKey[String]            = SettingKey[String]     ("stack-region",         "The region where the stacks are deployed. E.g. eu-west-1 ")
  lazy val stackName: SettingKey[String]              = SettingKey[String]     ("stack-name",           "stack name")
  lazy val stackAuth: SettingKey[StackAuth]           = SettingKey[StackAuth]  ("stack-auth",           "Sets AWS Credentials to be executed.")
  lazy val stackTemplateType: SettingKey[String]      = SettingKey[String]     ("stack-template-type",  "Sets the template type do be used for the stack S3Uri [static/dynamic]")
  lazy val stackTemplateS3Uri: SettingKey[URI]        = SettingKey[URI]        ("stack-template-s3url", "Template S3 Url Location.")

  lazy val stackSetup   : TaskKey[Unit]           = TaskKey[Unit]          ("stackSetup",    "Task used to setup a stack")
  lazy val stackTest    : TaskKey[Unit]           = TaskKey[Unit]          ("stackTest",     "Task used ti test a stack")
  lazy val stackTeardown: TaskKey[Unit]           = TaskKey[Unit]          ("stackTeardown", "Task used to teardown a stack")
  lazy val stackDescribe: TaskKey[Option[Stack]]  = TaskKey[Option[Stack]] ("stackDescribe", "Task used to describe stack completely")
  lazy val stackCreate  : TaskKey[Unit]           = TaskKey[Unit]          ("stackCreate",   "Task used to create a stack and returns its stackId")
  lazy val stackDelete  : TaskKey[Unit]           = TaskKey[Unit]          ("stackDelete",   "Task used to delete a stack")
  lazy val stackUpdate  : TaskKey[Unit]           = TaskKey[Unit]          ("stackUpdate",   "Task used to update a stack")
  lazy val stackPublish : TaskKey[URI]            = TaskKey[URI ]          ("stackPublish",  "Task used to publish the stack templates to s3 bucket")

  lazy val baseStackSettings: Seq[Setting[_]] = Seq(
    stackEnv           := "dev",
    stackName          := s"${name.value.replace("-service", "")}-${stackEnv.value}",
    stackParamsPath    := baseDirectory.value / "infrastructure" / stackTemplateType.value / "parameters",
    stackTemplatePath  := baseDirectory.value / "infrastructure" / stackTemplateType.value / "templates",
    stackSkip          := false,
    stackCustomParams  := Map.empty,
    stackTags          := Map("Billing" -> "Platforms"),
    stackCapabilities  := Seq("CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"),
    stackRegion        := "eu-west-1",
    stackAuth          := AuthProfile(),
    stackTemplateType  := Option(stackEnv.value).filter(_ == "static").getOrElse("dynamic"),
    stackTemplateS3Uri := uri(s"s3://artifacts-repo/${name.value}/${version.value}/cloudformation/${stackTemplateType.value}"),
    stackSetup         := {
      Def.taskDyn({
        stackDescribe.dependsOn(stackPublish).value
          .map(_.getStackStatus)
          .map({
            case "CREATE_COMPLETE" | "ROLLBACK_COMPLETE" | "UPDATE_COMPLETE" | "UPDATE_ROLLBACK_COMPLETE" =>
              Def.task { stackUpdate.value }
            case stackStatus =>
              sys.error(s"Invalid Stack State - '$stackStatus'")
          })
          .getOrElse{
            Def.task { stackCreate.value }
          }
      }).value
    },
    stackTest     := (test in IntegrationTest).value,
    stackTeardown := stackDelete.value,
    stackDescribe := StackDescribe(
      name   = stackName.value,
      region = stackRegion.value,
      auth   = stackAuth.value
    )(
      environment = stackEnv.value,
      logger      = streams.value.log
    ),
    stackCreate   := StackCreate(
      name          = stackName.value,
      region        = stackRegion.value,
      auth          = stackAuth.value,
      capabilities  = stackCapabilities.value,
      tags          = stackTags.value,
      paramsPath    = stackParamsPath.value,
      paramsCustom  = stackCustomParams.value,
      templateS3Uri = stackTemplateS3Uri.value
    )(
      environment = stackEnv.value,
      logger      = streams.value.log
    ),
    stackUpdate   := StackUpdate(
      name          = stackName.value,
      region        = stackRegion.value,
      auth          = stackAuth.value,
      capabilities  = stackCapabilities.value,
      tags          = stackTags.value,
      paramsPath    = stackParamsPath.value,
      paramsCustom  = stackCustomParams.value,
      templateS3Uri = stackTemplateS3Uri.value
    )(
      environment = stackEnv.value,
      logger      = streams.value.log
    ),
    stackDelete   := StackDelete(
      name   = stackName.value,
      region = stackRegion.value,
      auth   = stackAuth.value
    )(
      environment = stackEnv.value,
      logger      = streams.value.log
    ),
    stackPublish := StackPublish(
      region    = stackRegion.value,
      auth      = stackAuth.value,
      tempPath  = stackTemplatePath.value,
      tempS3Uri = stackTemplateS3Uri.value
    )(
      environment = stackEnv.value,
      logger      = streams.value.log
    )
  )
}

object PipelineKeys extends PipelineKeys
