package uk.co.telegraph.plugin

import sbt._
import org.json4s._
import org.json4s.native.JsonMethods._
import uk.co.telegraph.cloud._

import scala.language.implicitConversions
import scala.util.Try

package object pipeline {

  type StackParams         = Map[String, String]
  type StackTags           = Map[String, String]
  type StackAuth           = AuthCredentials
  type StackTemplateFormat = TemplateType

  import pipeline.PipelineConfigurations

  val DeployStatic  = PipelineConfigurations.DeployStatic
  val DeployProd    = PipelineConfigurations.DeployProd
  val DeployPreProd = PipelineConfigurations.DeployPreProd
  val DeployCt      = PipelineConfigurations.DeployCt
  val DeployDev     = PipelineConfigurations.DeployDev

  private [pipeline] def doGetTemplateFormat(templatePath:File):StackTemplateFormat = {
    def splitNameAndExtension: File => (String, String) = f => {
      val strSplit = f.getName.split('.')
      (strSplit.dropRight(1).mkString("."),strSplit.last)
    }

    Try{templatePath.listFiles()}.filter(_ != null ).toOption
      .flatMap { listFiles => listFiles
        .filter ( f => f != null && f.isFile && f.exists )
        .map    ( splitNameAndExtension )
        .collectFirst({
          case ("template", "json") => JsonFormat
          case ("template", "yaml") => YamlFormat
          case ("template", "yml") => YmlFormat
        })
      }
      .getOrElse{ JsonFormat }
  }

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
