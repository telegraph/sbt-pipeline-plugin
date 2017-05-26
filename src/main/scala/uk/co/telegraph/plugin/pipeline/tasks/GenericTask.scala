package uk.co.telegraph.plugin.pipeline.tasks

import sbt.Logger
import uk.co.telegraph.cloud._
import uk.co.telegraph.plugin.pipeline.StackAuth
import uk.co.telegraph.cloud.aws.{interpreter => awsInterpreter}

trait GenericTask {

  val taskName:String

  private [tasks] def logDetails(
    name      :StackName,
    region    :StackRegion,
    auth      :StackAuth,
    configOpt :Option[StackConfig] = None
  )(implicit environment:String, logger:Logger) = {
    logger.info(s"")
    logger.info(s"$taskName Stack:")
    logger.info(s"")
    logger.info(s"\t Environment: $environment")
    logger.info(s"\t Credentials: $auth")
    logger.info(s"\t      Region: $region")
    logger.info(s"\t        Name: $name")

    configOpt match {
      case Some(config) =>
        logger.info(s"\tCapabilities: ${config.capabilities}")
        logger.info(s"\t        Tags: ${config.tags}")
        logger.info(s"\t  Parameters: ${config.parameters}")
        logger.info(s"\tTemplate Uri: ${config.templateUri}")
      case None =>
    }
    logger.info(s"")
  }

  def interpreter(region:StackRegion, auth:StackAuth)(implicit logger:Logger) = awsInterpreter(region, auth)
}
