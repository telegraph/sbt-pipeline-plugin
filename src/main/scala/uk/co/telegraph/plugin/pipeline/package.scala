package uk.co.telegraph.plugin

import sbt._
import org.json4s._
import org.json4s.native.JsonMethods._
import uk.co.telegraph.cloud.AuthCredentials

import scala.language.implicitConversions

package object pipeline {

  type StackParams      = Map[String, String]
  type StackTags        = Map[String, String]
  type StackAuth        = AuthCredentials

  import pipeline.PipelineConfigurations

  val DeployStatic  = PipelineConfigurations.DeployStatic
  val DeployProd    = PipelineConfigurations.DeployProd
  val DeployPreProd = PipelineConfigurations.DeployPreProd
  val DeployDev     = PipelineConfigurations.DeployDev

  private [pipeline] def doReadParameters(parametersPath:File)(implicit environment:String):StackParams = {
    implicit val formats = DefaultFormats

    def processIfFolder:File => Option[File] = {
      case folder if folder.isDirectory => folder.listFiles().find(_.getName == s"parameters-$environment.json")
      case file => Some(file)
    }
    // SBT does not support JSON4S. We need to parse the file manually
    def readParameters(file:File):Map[String, String] = {
      val jsonData = parse(file)

      val parameters:List[(String, String)] = for{
        JObject(params) <- jsonData
        JField("ParameterKey",   JString(key  ))  <- params
        JField("ParameterValue", JString(value))  <- params
      } yield (key, value)
      parameters.toMap
    }

    Option(parametersPath).filter( _.exists ).flatMap( processIfFolder )
      .map( readParameters  ).getOrElse(Map.empty)
  }
}
