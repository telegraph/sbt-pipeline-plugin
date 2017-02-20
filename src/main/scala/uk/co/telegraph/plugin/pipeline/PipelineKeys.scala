package uk.co.telegraph.plugin.pipeline

import sbt._

trait PipelineKeys {

  //Stack Keys
  val stackEnv: SettingKey[String]               = SettingKey[String]     ("stack-env",           "Sets the infrastructure environment.")
  val stackParamsPath: SettingKey[File]          = SettingKey[File]       ("stack-path-params",   "Sets the infrastructure Static Path.")
  val stackTemplatePath: SettingKey[File]        = SettingKey[File]       ("stack-path-template", "Sets the infrastructure Template Path.")
  val stackSkip: SettingKey[Boolean]             = SettingKey[Boolean    ]("stack-static-skip",   "Skips the infrastructure deployment.")
  val stackCustomParams: SettingKey[StackParams] = SettingKey[StackParams]("stack-params",        "Sets Custom Infrastructure Parameters")
  val stackTags: SettingKey[StackTags]           = SettingKey[StackTags  ]("stack-tags",          "Tags of this stack")
  val stackCapabilities: SettingKey[Seq[String]] = SettingKey[Seq[String]]("stack-capabilities",  "The list of capabilities that you want to allow in the stack . E.g.[CAPABILITY_IAM]")
  val stackRegion: SettingKey[String]            = SettingKey[String]     ("stack-region",        "The region where the stacks are deployed. E.g. eu-west-1 ")
  val stackName: SettingKey[String]              = SettingKey[String]     ("stack-name",          "stack name")
  val stackAuth: SettingKey[StackAuth]           = SettingKey[StackAuth]  ("stack-auth",          "Sets AWS Credentials to be executed.")

  val stackSetup   : TaskKey[Unit]           = TaskKey[Unit]          ("stackSetup",    "Task used to setup a stack")
  val stackTest    : TaskKey[Unit]           = TaskKey[Unit]          ("stackTest",     "Task used ti test a stack")
  val stackTeardown: TaskKey[Unit]           = TaskKey[Unit]          ("stackTeardown", "Task used to teardown a stack")
  val stackDescribe: TaskKey[Option[String]] = TaskKey[Option[String]]("stackDescribe", "Task used to describe stack completely")
  val stackCreate  : TaskKey[Unit]           = TaskKey[Unit]          ("stackCreate",   "Task used to create a stack and returns its stackId")
  val stackDelete  : TaskKey[Unit]           = TaskKey[Unit]          ("stackDelete",   "Task used to delete a stack")
  val stackUpdate  : TaskKey[Unit]           = TaskKey[Unit]          ("stackUpdate",   "Task used to update a stack")
  val stackPublish : TaskKey[URI]            = TaskKey[URI ]          ("stackPublish",  "Task used to publish the stack templates to s3 bucket")
}

object PipelineKeys extends PipelineKeys
