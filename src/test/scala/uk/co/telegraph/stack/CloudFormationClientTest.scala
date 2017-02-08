package uk.co.telegraph.stack

import com.amazonaws.services.cloudformation.AmazonCloudFormation
import com.amazonaws.services.cloudformation.model._
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mkEq}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}
import org.scalatest.junit.JUnitRunner
import uk.co.telegraph.plugin.pipeline._
import uk.co.telegraph.stack.auth.{AuthProfile, AuthProvider}
import uk.co.telegraph.stack.auth.AuthProvider._

import scala.util.{Failure, Success, Try}
import sbt._

/**
 * Created: rodriguesa 
 * Date   : 04/02/2017
 * Project: sbt-pipeline-plugin
 */
@RunWith(classOf[JUnitRunner])
class CloudFormationClientTest extends FunSpec with Matchers with BeforeAndAfter {

  import CloudFormationClientTest._

  before {
    reset(CfClientMock)
  }

  describe("Given the 'CloudFormationClient', "){

    it("I should be able to collect information from an already deployed stack"){
      when( CfClientMock.describeStacks(mkEq(SampleDescribeRequest)) ).thenReturn( SampleDescribeResult )

      val result = CloudFormationClientObj.describe(SampleStackName)
      result shouldBe Success(SampleStack)
    }

    it("I should be able to delete a stack"){
      when(CfClientMock.deleteStack( mkEq(SampleDeleteRequest) )).thenReturn( new DeleteStackResult())

      Try{
        CloudFormationClientObj.delete(SampleStackName)
      } match {
        case Success(_) =>
        case Failure(_) => fail()
      }
    }

    it("I should be able to create a stack"){
      when(CfClientMock.describeStacks(mkEq(SampleDescribeRequest)) ).thenReturn( new DescribeStacksResult() )
      when(CfClientMock.createStack(mkEq(SampleCreateRequest)))
        .thenReturn( SampleCreateResult )

      val result = CloudFormationClientObj.createOrUpdate(
        name         = SampleStackName,
        capabilities = SampleStackCapabilities,
        templateUri  = SampleTemplateUri,
        tags         = SampleStackTags,
        parameters   = SampleStackParams
      )
      result shouldBe Success(SampleStackId)
    }

    it("I should be able to update a stack"){
      when(CfClientMock.describeStacks(mkEq(SampleDescribeRequest)) ).thenReturn( SampleDescribeResult )
      when(CfClientMock.updateStack(mkEq(SampleUpdateRequest)))
        .thenReturn( SampleUpdateResult )

      val result = CloudFormationClientObj.createOrUpdate(
        name         = SampleStackName,
        capabilities = SampleStackCapabilities,
        templateUri  = SampleTemplateUri,
        tags         = SampleStackTags,
        parameters   = SampleStackParams
      )
      result shouldBe Success(SampleStackId)
    }

    it("I should be able to get status from a stack"){
      when(CfClientMock.describeStacks(mkEq(SampleDescribeRequest)) ).thenReturn( SampleDescribeResult )

      val result = CloudFormationClientObj.status(SampleStackName)
      result shouldBe Some(SampleStackStatus)
    }

    it("I should get no status if the stack does not exist"){
      when(CfClientMock.describeStacks(mkEq(SampleDescribeRequest)) ).thenReturn( new DescribeStacksResult() )

      val result = CloudFormationClientObj.status(SampleStackName)
      result shouldBe None
    }
  }
}

object CloudFormationClientTest {

  val CfClientMock            = mock(classOf[AmazonCloudFormation])

  val SampleStackId           = "stack-1"
  val SampleStackName         = "sbt-pipeline-plugin"
  val SampleS3Bucket          = "artifacts-repo"
  val SampleS3Key             = "test"
  val SampleStackCapabilities = Seq("CAPABILITY_IAM", "CAPABILITY_NAMED_IAM")
  val SampleTemplateUri       = uri(s"s3://$SampleS3Bucket/$SampleS3Key/template.json")
  val SampleStackTags         = Map(
    "billing" -> "platforms"
  )
  val SampleStackParams       = Map(
    "Environment" -> "prod"
  )
  val SampleStackStatus       = "CREATE_COMPLETE"

  val SampleStack             = new Stack()
    .withStackId    (SampleStackId)
    .withStackName  (SampleStackName)
    .withStackStatus(SampleStackStatus)

  val SampleDescribeRequest   = new DescribeStacksRequest().withStackName(SampleStackName)
  val SampleDescribeResult    = new DescribeStacksResult().withStacks(SampleStack)
  val SampleDeleteRequest     = new DeleteStackRequest().withStackName(SampleStackName)

  val SampleCreateRequest     = new CreateStackRequest()
    .withCapabilities(SampleStackCapabilities: _*)
    .withStackName   (SampleStackName)
    .withTags        (SampleStackTags  .toSeq.map( x => new Tag      ().withKey(x._1).withValue(x._2) ): _*)
    .withParameters  (SampleStackParams.toSeq.map( x => new Parameter().withParameterKey(x._1).withParameterValue(x._2) ): _* )
    .withTemplateURL (s"https://s3-eu-west-1.amazonaws.com/$SampleS3Bucket/$SampleS3Key/template.json")
  val SampleCreateResult      = new CreateStackResult().withStackId(SampleStackId)

  val SampleUpdateRequest     = new UpdateStackRequest()
    .withCapabilities(SampleStackCapabilities: _*)
    .withStackName   (SampleStackName)
    .withTags        (SampleStackTags  .toSeq.map( x => new Tag      ().withKey(x._1).withValue(x._2) ): _*)
    .withParameters  (SampleStackParams.toSeq.map( x => new Parameter().withParameterKey(x._1).withParameterValue(x._2) ): _* )
    .withTemplateURL (s"https://s3-eu-west-1.amazonaws.com/$SampleS3Bucket/$SampleS3Key/template.json")
  val SampleUpdateResult      = new UpdateStackResult().withStackId(SampleStackId)

  object CloudFormationClientObj extends CloudFormationClient {
    val authCredentials: StackAuth = AuthProfile()
    val region:String             = "eu-west-1"
    override lazy val cfClient    = CfClientMock
  }
}
