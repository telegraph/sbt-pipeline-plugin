import uk.co.telegraph.stack.{CloudFormationClient, S3Client}
import uk.co.telegraph.stack.auth._

import sbt._

/**
 * Created: rodriguesa 
 * Date   : 04/02/2017
 * Project: sbt-pipeline-plugin
 */
object scrap extends App{

  val s3Client = S3Client(AuthProfile(), "eu-west-1")
  val cfClient = CloudFormationClient(AuthProfile(), "eu-west-1")

  val SampleStackName         = "sbt-pipeline-plugin"
  val SampleS3Bucket          = "artifacts-repo"
  val SampleS3Key             = "test1"
  val SampleStackCapabilities = Seq("CAPABILITY_IAM", "CAPABILITY_NAMED_IAM")
  val SampleTemplateUri       = uri(s"s3://$SampleS3Bucket/$SampleS3Key/template.json")
  val SampleStackTags         = Map(
    "billing" -> "platforms"
  )
  val SampleStackParams       = Map(
    "NumberWithRange" -> "5"
  )
//  s3Client.publish(uri("s3://artifacts-repo/test1"), file("./src/test/resources/cloudformation/templates"))

  val result = cfClient.createOrUpdate(
    name         = SampleStackName,
    capabilities = SampleStackCapabilities,
    templateUri  = SampleTemplateUri,
    tags         = SampleStackTags,
    parameters   = SampleStackParams
  )

  cfClient.delete(SampleStackName)
  println("test " + result)
}
