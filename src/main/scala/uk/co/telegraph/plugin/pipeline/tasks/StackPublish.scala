package uk.co.telegraph.plugin.pipeline.tasks

import sbt.{File, Logger, URI}
import uk.co.telegraph.cloud.StackRegion
import uk.co.telegraph.cloud.dls.pushTemplate
import uk.co.telegraph.plugin.pipeline.StackAuth
import scala.util.{Failure, Success, Try}

trait StackPublish extends GenericTask {

  override val taskName = "S3 Publish"

  def apply(
    region   : StackRegion,
    auth     : StackAuth,
    tempPath : File,
    tempS3Uri: URI
  )(implicit environment:String, logger:Logger):URI = {
    logDetails(region, auth, tempPath, tempS3Uri)

    Try{
      pushTemplate(tempS3Uri, tempPath)
        .foldMap(interpreter(region, auth))
    } match {
      case Success(_) =>
        tempS3Uri
      case Failure(ex) =>
        sys.error(s"ERROR: Fail during 'stackPublish' - ${ex.getMessage}")
    }
  }

  private [tasks] def logDetails(
    region   : StackRegion,
    auth     : StackAuth,
    tempPath : File,
    tempS3Uri: URI
  )(implicit environment:String, logger:Logger) = {
    logger.info(s"")
    logger.info(s"$taskName Stack:")
    logger.info(s"")
    logger.info(s"\t Environment: $environment")
    logger.info(s"\t Credentials: ${auth.getClass.getSimpleName}")
    logger.info(s"\t      Region: $region")
    logger.info(s"\tTemplatePath: $tempPath")
    logger.info(s"\t      S3 Uri: $tempS3Uri")
    logger.info(s"")
  }
}

object StackPublish extends StackPublish