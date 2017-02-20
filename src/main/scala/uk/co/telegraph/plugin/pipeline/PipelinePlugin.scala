package uk.co.telegraph.plugin.pipeline

import sbt._
import sbt.Keys._
import sbt.Project.inConfig
import uk.co.telegraph.cloud.aws.interpreter

import scala.util.{Failure, Success, Try}
import uk.co.telegraph.cloud._
import uk.co.telegraph.cloud.dls._

object PipelinePlugin extends AutoPlugin{

  object autoImport extends PipelineKeys with PipelineConfigurations

  import autoImport._

  override def trigger: PluginTrigger = allRequirements

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
      val parameters  = doReadParameters(parametersPath) ++ parametersCustom
      val config      = StackConfig(capabilities, templateUri, tags, parameters)

      log.info(s"")
      log.info(s"Setup Stack [Create Or Update]:")
      log.info(s"")
      logDetails(environment, authCredentials, region, name, Some(config))
      log.info("")


      Try{
        val request = createOrUpdate(name, config).flatMap( _ => await(name) )
        request.foldMap(interpreter(region, authCredentials))
      } match {
        case Success(_) =>
        case Failure(ex) =>
          log.error("")
          log.error(s" ERROR: Fail during 'stackSetup' - ${ex.getMessage}")
          log.error("")
          fail( ex )
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
      logDetails(environment, authCredentials, region, name, None)
      log.info(s"")

      Try{
        describe(name)
          .foldMap(interpreter(region, authCredentials))
          .map    (_.getStackName)
      } get
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
      val parameters  = doReadParameters(parametersPath) ++ parametersCustom
      val config      = StackConfig(capabilities, templateUri, tags, parameters)

      log.info(s"")
      log.info(s"Creating Stack:")
      log.info(s"")
      logDetails(environment, authCredentials, region, name, Some(config))
      log.info(s"")

      Try{
        val request = create(name, config).flatMap( _ => await(name) )
        request.foldMap(interpreter(region, authCredentials))
      } match {
        case Success(_) =>
        case Failure(ex) =>
          log.error("")
          log.error(s" ERROR: Fail during 'stackCreate' - ${ex.getMessage}")
          log.error("")
          fail()
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
      logDetails(environment, authCredentials, region, name, None)
      log.info("")

      Try{
        val request = delete(name).flatMap(_ => await(name))
        request.foldMap(interpreter(region, authCredentials))
      }
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
      val parameters  = doReadParameters(parametersPath) ++ parametersCustom
      val config      = StackConfig(capabilities, templateUri, tags, parameters)

      log.info(s"")
      log.info(s"Updating Stack:")
      log.info(s"")
      logDetails(environment, authCredentials, region, name, Some(config))
      log.info(s"")

      Try{
        val request = dls.update(name, config).flatMap( _ => await(name) )
        request.foldMap(interpreter(region, authCredentials))
      } match {
        case Success(_) =>
        case Failure(ex) =>
          log.error("")
          log.error(s" ERROR: Fail during 'stackUpdate' - ${ex.getMessage}")
          log.error("")
          fail( ex )
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

      Try{
        val request = pushTemplate(s3Uri, templatePath)
        request.foldMap(interpreter(region, authCredentials))
      } match {
        case Success(_) =>
          s3Uri
        case Failure(ex) =>
          log.error("")
          log.error(s" ERROR: Fail during 'stackPublish' - ${ex.getMessage}")
          log.error("")
          fail()
      }
    }
  )

  override lazy val projectSettings: Seq[Setting[_]] =
    inConfig(autoImport.DeployStatic )(basePipelineTasks ++ staticDeploySettings ) ++
    inConfig(autoImport.DeployProd   )(basePipelineTasks ++ prodDeploySettings   ) ++
    inConfig(autoImport.DeployPreProd)(basePipelineTasks ++ preprodDeploySettings) ++
    inConfig(autoImport.DeployDev    )(basePipelineTasks ++ devDeploySettings    ) ++
    (basePipelineTasks ++ devDeploySettings)

  private def logDetails(
    environment:String,
    authCredentials:AuthCredentials,
    region:StackRegion,
    name:StackName,
    configOpt:Option[StackConfig] = None
  )(implicit log:Logger) = {
    log.info(s"\t Environment: $environment")
    log.info(s"\t Credentials: ${authCredentials.getClass.getSimpleName}")
    log.info(s"\t      Region: $region")
    log.info(s"\t        Name: $name")

    configOpt match {
      case Some(config) =>
        log.info(s"\tCapabilities: ${config.capabilities}")
        log.info(s"\t        Tags: ${config.tags}")
        log.info(s"\t  Parameters: ${config.parameters}")
        log.info(s"\tTemplate Uri: ${config.templateUri}")
      case None =>
    }
  }
}
