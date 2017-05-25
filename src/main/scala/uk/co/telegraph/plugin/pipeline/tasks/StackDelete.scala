package uk.co.telegraph.plugin.pipeline.tasks

import sbt.Logger
import uk.co.telegraph.cloud.dls.{await, delete}
import uk.co.telegraph.cloud.{StackName, StackRegion}
import uk.co.telegraph.plugin.pipeline.StackAuth

import scala.util.Try

trait StackDelete extends GenericTask {

  override val taskName = "Delete"

  def apply(name:StackName, region:StackRegion, auth:StackAuth)(implicit environment:String, logger:Logger):Unit = {
    logDetails(name, region, auth)
    Try{
      delete(name)
        .flatMap(_ => await(name))
        .foldMap(interpreter(region, auth))
    }
  }
}

object StackDelete extends StackDelete
