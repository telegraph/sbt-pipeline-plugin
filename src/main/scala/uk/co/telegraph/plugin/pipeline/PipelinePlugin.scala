package uk.co.telegraph.plugin.pipeline

import sbt.Keys._
import sbt.Project.inConfig
import sbt._
import uk.co.telegraph.stack.auth.AuthProfile
import uk.co.telegraph.stack.{CloudFormationClient, S3Client}

import scala.util.{Failure, Success}

object PipelinePlugin extends AutoPlugin{

  object autoImport extends PipelineKeys with PipelineConfigurations

  import autoImport._

  override def trigger = allRequirements

  lazy val defaultDeploySettings: Seq[Setting[_]] = Seq(
    stackEnv          := "dev",
    stackName         := s"${name.value.replace("-service", "")}-${stackEnv.value}",
    stackParamsPath   := baseDirectory.value / "infrastructure" / "dynamic" / "parameters",
    stackTemplatePath := baseDirectory.value / "infrastructure" / "dynamic" / "templates",
    stackSkip         := false,
    stackCustomParams := Map.empty,
    stackTags         := Map("Billing" -> "Platforms"),
    stackCapabilities := Seq("CAPABILITY_IAM", "CAPABILITY_NAMED_IAM"),
    stackRegion       := "eu-west-1",
    stackAuth         := AuthProfile()
  )

  lazy val staticDeploySettings : Seq[Setting[_]] = defaultDeploySettings ++ Seq(
    stackEnv          := "static",
    stackParamsPath   := baseDirectory.value / "infrastructure" / "static" / "parameters",
    stackTemplatePath := baseDirectory.value / "infrastructure" / "static" / "templates"
  )

  lazy val devDeploySettings    : Seq[Setting[_]] = defaultDeploySettings
  lazy val preprodDeploySettings: Seq[Setting[_]] = defaultDeploySettings ++ Seq(
    stackEnv   := "preprod"
  )
  lazy val prodDeploySettings: Seq[Setting[_]] = defaultDeploySettings ++ Seq(
    stackEnv  := "prod"
  )

  //TODO: Refactor Stack Tasks
  lazy val basePipelineTasks: Seq[Setting[_]] = Seq(
    stackSetup    := {
      implicit val environment = (stackEnv          in stackSetup).value
      implicit val log = streams.value.log

      val authCredentials  = (stackAuth         in stackSetup).value
      val region           = (stackRegion       in stackSetup).value
      val name             = (stackName         in stackSetup).value
      val capabilities     = (stackCapabilities in stackSetup).value
      val tags             = (stackTags         in stackSetup).value
      val parametersPath   = (stackParamsPath   in stackSetup).value
      val parametersCustom = (stackCustomParams in stackSetup).value
      val s3Uri            = (stackPublish      in stackSetup).value

      // Compute the main CloudFormation template
      val templateUri      = Option(s3Uri)
        .filterNot( x => x.getPath.endsWith(".json"))
        .map      ( x => uri(s"${x.toString}/template.json"))
        .getOrElse( s3Uri )
      // Compute the Parameters
      val parameters       = doReadParameters(parametersPath) ++ parametersCustom

      log.info(s"")
      log.info(s"Setup Stack [Create Or Update]:")
      log.info(s"")
      log.info(s"\t Environment: $environment")
      log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
      log.info(s"\t      Region: $region")
      log.info(s"\t        Name: $name")
      log.info(s"\tCapabilities: $capabilities")
      log.info(s"\t        Tags: $tags")
      log.info(s"\t  Parameters: $parameters")
      log.info(s"\tTemplate Uri: $templateUri")
      log.info("")

      val taskResult = CloudFormationClient(authCredentials, region).createOrUpdate(
        name         = name,
        capabilities = capabilities,
        templateUri  = templateUri,
        tags         = tags,
        parameters   = parameters
      )
      taskResult match {
        case Success(_) =>
        case Failure(ex) => fail(ex.getMessage)
      }
    },
    stackTest     := (test in IntegrationTest).value,
    stackTeardown := (stackDelete in stackTeardown).value,

    stackDescribe := {
      implicit val environment = (stackEnv in stackDescribe).value
      implicit val log = streams.value.log

      val authCredentials  = (stackAuth    in stackDescribe).value
      val region           = (stackRegion  in stackDescribe).value
      val name             = (stackName    in stackDescribe).value

      log.info(s"")
      log.info(s"Describe Stack:")
      log.info(s"")
      log.info(s"\t Environment: $environment")
      log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
      log.info(s"\t      Region: $region")
      log.info(s"\t        Name: $name")
      log.info(s"")

      CloudFormationClient(authCredentials, region).describe(name).map( _.getStackId ).toOption
    },
    stackCreate   := {
      implicit val environment = (stackEnv          in stackCreate).value
      implicit val log = streams.value.log

      val authCredentials  = (stackAuth         in stackCreate).value
      val region           = (stackRegion       in stackCreate).value
      val name             = (stackName         in stackCreate).value
      val capabilities     = (stackCapabilities in stackCreate).value
      val tags             = (stackTags         in stackCreate).value
      val parametersPath   = (stackParamsPath   in stackCreate).value
      val parametersCustom = (stackCustomParams in stackCreate).value
      val s3Uri            = (stackPublish      in stackCreate).value

      // Compute the main CloudFormation template
      val templateUri      = Option(s3Uri)
        .filterNot( x => x.getPath.endsWith(".json"))
        .map      ( x => uri(s"${x.toString}/template.json"))
        .getOrElse( s3Uri )
      // Compute the Parameters
      val parameters       = doReadParameters(parametersPath) ++ parametersCustom

      log.info(s"")
      log.info(s"Creating Stack:")
      log.info(s"")
      log.info(s"\t Environment: $environment")
      log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
      log.info(s"\t      Region: $region")
      log.info(s"\t        Name: $name")
      log.info(s"\tCapabilities: $capabilities")
      log.info(s"\t        Tags: $tags")
      log.info(s"\t  Parameters: $parameters")
      log.info(s"\tTemplate Uri: $templateUri")
      log.info("")

      val taskResult = CloudFormationClient(authCredentials, region).create(
          name         = name,
          capabilities = capabilities,
          templateUri  = templateUri,
          tags         = tags,
          parameters   = parameters
        )
      taskResult match {
        case Success(_) =>
        case Failure(ex) => fail(ex.getMessage)
      }
    },
    stackDelete   := {
      implicit val environment = (stackEnv          in stackDelete).value
      implicit val log = streams.value.log
      val authCredentials = (stackAuth         in stackDelete).value
      val region          = (stackRegion       in stackDelete).value
      val name            = (stackName         in stackDelete).value

      log.info(s"")
      log.info(s"Deleting Stack:")
      log.info(s"")
      log.info(s"\t Environment: $environment")
      log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
      log.info(s"\t      Region: $region")
      log.info(s"\t        Name: $name")
      log.info("")

      CloudFormationClient(authCredentials, region).delete(name)
    },
    stackUpdate   := {
      implicit val environment = (stackEnv          in stackUpdate).value
      implicit val log = streams.value.log

      val authCredentials  = (stackAuth         in stackUpdate).value
      val region           = (stackRegion       in stackUpdate).value
      val name             = (stackName         in stackUpdate).value
      val capabilities     = (stackCapabilities in stackCreate).value
      val tags             = (stackTags         in stackUpdate).value
      val parametersPath   = (stackParamsPath   in stackUpdate).value
      val parametersCustom = (stackCustomParams in stackUpdate).value
      val s3Uri            = (stackPublish      in stackUpdate).value

      // Compute the main CloudFormation template
      val templateUri      = Option(s3Uri)
        .filterNot( x => x.getPath.endsWith(".json"))
        .map      ( x => uri(s"${x.toString}/template.json"))
        .getOrElse( s3Uri )
      // Compute the Parameters
      val parameters       = doReadParameters(parametersPath) ++ parametersCustom

      log.info(s"")
      log.info(s"Updating Stack:")
      log.info(s"")
      log.info(s"\t Environment: $environment")
      log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
      log.info(s"\t      Region: $region")
      log.info(s"\t        Name: $name")
      log.info(s"\tCapabilities: $capabilities")
      log.info(s"\t        Tags: $tags")
      log.info(s"\t  Parameters: $parameters")
      log.info(s"\tTemplate Uri: $templateUri")
      log.info("")

      val taskResult = CloudFormationClient(authCredentials, region).create(
        name         = name,
        capabilities = capabilities,
        templateUri  = templateUri,
        tags         = tags,
        parameters   = parameters
      )
      taskResult match {
        case Success(_) =>
        case Failure(ex) => fail(ex.getMessage)
      }
    },

    stackPublish := {
      implicit val environment = (stackEnv          in stackPublish).value
      implicit val log = streams.value.log
      val authCredentials = (stackAuth         in stackPublish).value
      val region          = (stackRegion       in stackPublish).value
      val templatePath    = (stackTemplatePath in stackPublish).value
      val projectName     = (name              in stackPublish).value
      val projectVersion  = (version           in stackPublish).value
      val templateTypes   = Option(environment).filter(_ == "static").getOrElse("dynamic")
      val s3Uri           = uri(s"s3://artifacts-repo/$projectName/$projectVersion/cloudformation/$templateTypes")

      log.info(s"")
      log.info(s"Publishing Stack to S3 Bucket:")
      log.info(s"")
      log.info(s"\t Environment: $environment")
      log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
      log.info(s"\t      Region: $region")
      log.info(s"\tTemplatePath: $templatePath")
      log.info(s"\t      S3 Uri: $s3Uri")
      log.info("")
      S3Client(authCredentials, region).publish(s3Uri, templatePath) match {
        case Success(_)  => s3Uri
        case Failure(ex) =>
          log.error(s"Failed to Publish Stack Templates: ${ex.getMessage}")
          fail()
      }
    }
  )

  override lazy val projectSettings =
    inConfig(autoImport.DeployStatic )(basePipelineTasks ++ staticDeploySettings ) ++
    inConfig(autoImport.DeployProd   )(basePipelineTasks ++ prodDeploySettings   ) ++
    inConfig(autoImport.DeployPreProd)(basePipelineTasks ++ preprodDeploySettings) ++
    inConfig(autoImport.DeployDev    )(basePipelineTasks ++ devDeploySettings    ) ++
    (basePipelineTasks ++ devDeploySettings)

}
