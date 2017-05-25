package uk.co.telegraph.plugin.pipeline.tasks

import sbt.{File, Logger, URI, uri}
import uk.co.telegraph.cloud.dls.{await, create}
import uk.co.telegraph.cloud.{StackConfig, StackName, StackRegion}
import uk.co.telegraph.plugin.pipeline.{StackAuth, StackParams, StackTags, doReadParameters}

import scala.util.{Failure, Success, Try}

trait StackCreate extends GenericTask{

  val taskName = "Create"

  def apply(
    name:StackName,
    region:StackRegion,
    auth:StackAuth,
    capabilities:Seq[String],
    tags:StackTags,
    paramsPath:File,
    paramsCustom:StackParams,
    templateS3Uri:URI
  )(implicit environment:String, logger:Logger):Unit = {
    val templateUri = uri(s"${templateS3Uri.toString}/template.json")
    // Compute the Parameters
    val parameters  = doReadParameters(paramsPath) ++ paramsCustom
    val config      = StackConfig(capabilities, templateUri, tags, parameters)

    logDetails(name, region, auth, Some(config))
    Try{
      create(name, config)
        .flatMap( _ => await(name) )
        .foldMap(interpreter(region, auth))
    } match {
      case Success(stackStatus) =>
        stackStatus.orElse(Some("UNKNOWN")) match {
          case Some("CREATE_COMPLETE") =>
            logger.info("Stack Deployed Successfully")
          case Some(status) =>
            sys.error(s"ERROR: Stack Deployment failed - '$status'")
          case _ =>
        }
      case Failure(ex) =>
        sys.error(s"ERROR: Fail during 'stackCreate' - ${ex.getMessage}")
    }
  }
}

object StackCreate extends StackCreate