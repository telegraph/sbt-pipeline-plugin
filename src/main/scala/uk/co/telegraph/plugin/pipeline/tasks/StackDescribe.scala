package uk.co.telegraph.plugin.pipeline.tasks

import com.amazonaws.services.cloudformation.model.Stack
import sbt.Logger
import uk.co.telegraph.cloud.dls.describe
import uk.co.telegraph.cloud.{StackName, StackRegion}
import uk.co.telegraph.plugin.pipeline.StackAuth
import scala.language.postfixOps
import scala.util.Try

trait StackDescribe extends GenericTask{

  override val taskName = "Describe"

  def apply(name:StackName,  region:StackRegion, auth:StackAuth)(implicit environment:String, logger:Logger):Option[Stack] = {
    logDetails(name, region, auth)
    Try{
      describe(name).foldMap(interpreter(region, auth))
    } getOrElse None
  }
}

object StackDescribe extends StackDescribe
