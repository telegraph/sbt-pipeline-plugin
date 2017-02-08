package uk.co.telegraph.stack

import com.amazonaws.services.cloudformation.AmazonCloudFormationClientBuilder
import com.amazonaws.services.cloudformation.model._
import sbt._
import uk.co.telegraph.plugin.pipeline._
import uk.co.telegraph.stack.auth.AuthCredentials

import scala.collection.convert.wrapAsScala._
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try

/**
 * Created: rodriguesa 
 * Date   : 03/02/2017
 * Project: sbt-pipeline-plugin
 */
trait CloudFormationClient extends ClientWithAuth{

  import CloudFormationClient._

  private [stack] lazy val cfClient = AmazonCloudFormationClientBuilder.standard()
    .withRegion     ( region )
    .withCredentials( authProvider )
    .build()

  def createOrUpdate(name:String, capabilities:Seq[String], templateUri:URI, tags:StackTags, parameters:StackParams):Try[String] = {
    describe(name) transform(
      _ => update(name, capabilities, templateUri, tags, parameters),
      _ => create(name, capabilities, templateUri, tags, parameters)
    )
  }

  /**
   * Creates a Stack
   */
  def create(name:String, capabilities:Seq[String], templateUri:URI, tags:StackTags, parameters:StackParams):Try[String] = {
    val request = new CreateStackRequest()
      .withStackName   ( name             )
      .withTemplateURL ( templateUri      )
      .withCapabilities( capabilities: _* )
      .withParameters  ( parameters map toModelParameter toSeq: _* )
      .withTags        ( tags       map toModelTag       toSeq: _* )

    Try{ cfClient.createStack( request ).getStackId }
  }

  /**
   * Returns information of a certain stack.
   */
  def describe(name:String):Try[Stack] = {
    val request  = new DescribeStacksRequest().withStackName(name)
    Try{ cfClient.describeStacks(request) } map { _.getStacks.head }
  }

  /**
   * Deletes a Stack
   */
  def delete(name:String):Unit = {
    val request = new DeleteStackRequest().withStackName(name)

    Try{ cfClient.deleteStack(request) }
  }

  /**
   * Updates the stack and returns the Stack Id
   */
  def update(name:String, capabilities:Seq[String], templateUri:URI, tags:StackTags, parameters:StackParams):Try[String] = {
    val request = new UpdateStackRequest()
      .withStackName   ( name             )
      .withTemplateURL ( templateUri      )
      .withCapabilities( capabilities: _* )
      .withParameters  ( parameters map toModelParameter toSeq: _* )
      .withTags        ( tags       map toModelTag       toSeq: _* )

    Try{ cfClient.updateStack( request ).getStackId }
  }

  /**
   * Returns the Stack Status or None if the stack does not exist
   */
  def status(name:String):Option[String] = {
    describe(name).toOption.map( _.getStackStatus )
  }

  def wait(name:String):Option[String] = {
    def statuses:Stream[String] = Stream.cons( status(name).orNull, statuses )

    statuses.takeWhile(s => Option(s).exists(_.endsWith("_PROGRESS")))
      .foreach( _ => Thread.sleep(1000))

    statuses.headOption
  }

  /**
   * Map a tuple to a Model Tag
   */
  private def toModelTag(tag:(String, String)):Tag = {
    new Tag().withKey(tag._1).withValue(tag._2)
  }

  /**
   * Map a tuple to a Model Parameter
   */
  private def toModelParameter(parameter:(String, String)):Parameter = {
    new Parameter().withParameterKey(parameter._1).withParameterValue(parameter._2)
  }
}

object CloudFormationClient {

  private case class CloudFormationClientImp(authCredentials: AuthCredentials, region:String) extends CloudFormationClient

  implicit def s3UriToUrl(templateUri:URI):String = {
    s"https://s3-eu-west-1.amazonaws.com/${templateUri.getHost}${templateUri.getPath}"
  }
  def apply(authCredentials:AuthCredentials, region:String):CloudFormationClient = {
    CloudFormationClientImp(authCredentials,region)
  }
}