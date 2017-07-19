package uk.co.telegraph.cloud.aws

import java.lang.Thread.sleep

import com.amazonaws.services.cloudformation.{AmazonCloudFormation, AmazonCloudFormationClientBuilder}
import com.amazonaws.services.cloudformation.model._
import sbt.{Logger, URI}
import uk.co.telegraph.cloud.{AuthCredentials, StackConfig, StackName, StackStatus}

import scala.collection.convert.WrapAsScala._
import scala.language.{implicitConversions, postfixOps}
import scala.util.Try
import AwsCloudFormation._

private [aws] trait AwsCloudFormation { this: AwsClientWithAuth =>

  lazy val cfClient: AmazonCloudFormation = AmazonCloudFormationClientBuilder.standard()
    .withRegion     ( region )
    .withCredentials( authProvider )
    .build()

  /**
    * Method used to create to Describe an AWS Stack
    */
  def describe(name:StackName):Option[Stack] = {
    val request = new DescribeStacksRequest()
      .withStackName(name)

    Try{ cfClient.describeStacks(request).getStacks }.toOption.flatMap( _.headOption )
  }

  /**
    * Method used to create an AWS Stack
    */
  def create(name:StackName, config:StackConfig):Unit = {
    val request = new CreateStackRequest()
      .withStackName   ( name               )
      .withTemplateURL ( config.templateUri )
      .withCapabilities( config.capabilities: _* )
      .withParameters  ( config.parameters map toModelParameter toSeq: _* )
      .withTags        ( config.tags       map toModelTag       toSeq: _* )

    cfClient.createStack( request )
  }

  /**
    * Method used to update an AWS Stack
    */
  def update(name:StackName, config:StackConfig):Unit = {
    val request = new UpdateStackRequest()
      .withStackName   ( name               )
      .withTemplateURL ( config.templateUri )
      .withCapabilities( config.capabilities: _* )
      .withParameters  ( config.parameters map toModelParameter toSeq: _* )
      .withTags        ( config.tags       map toModelTag       toSeq: _* )

    cfClient.updateStack( request )
  }

  /**
    * Method used to delete an AWS Stack
    */
  def delete(name:StackName):Unit = {
    val request = new DeleteStackRequest()
      .withStackName(name)

    cfClient.deleteStack( request )
  }

  /**
    * Method used to await until an AWS Stack finishes the deployment process
    */
  def await(name:StackName):Option[StackStatus] = {

    def statuses: Stream[String] = Stream.cons(
      describe(name)
        .map( stack => {
          val status = stack.getStackStatus
          log.info(s"\t  Stack Status: $status")
          status
        })
        .orNull,
      statuses
    )

    statuses
      .takeWhile( s => Option(s).exists(_.endsWith("_PROGRESS")) )
      .foreach( _ => sleep(AwaitThrottle) )

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

object AwsCloudFormation{

  val AwaitThrottle = 5000
  val S3UrlPrefix   = "https://s3-eu-west-1.amazonaws.com"

  /**
    * Transform an S3 Uri into Url
    */
  implicit def s3UriToUrl(templateUri:URI):String = {
    s"$S3UrlPrefix/${templateUri.getHost}${templateUri.getPath}"
  }

  private case class AwsCloudFormationImp(region:String, authCredentials: AuthCredentials, log: Logger)
    extends AwsCloudFormation with AwsClientWithAuth

  def apply(region:String, authCredentials: AuthCredentials)(implicit log:Logger):AwsCloudFormation = {
    AwsCloudFormationImp(region, authCredentials, log)
  }
}

