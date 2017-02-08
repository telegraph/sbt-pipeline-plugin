package uk.co.telegraph.stack

import com.amazonaws.services.s3.transfer.{MultipleFileUpload, TransferManager, Upload}
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mkEq}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import sbt._
import uk.co.telegraph.plugin.pipeline._
import uk.co.telegraph.stack.S3Client.{S3InvalidLocalPath, S3InvalidProtocol}
import uk.co.telegraph.stack.auth._

import scala.util.{Failure, Success}

@RunWith(classOf[JUnitRunner])
class S3ClientTest extends FunSpec with Matchers with BeforeAndAfter {

  import S3ClientTest._

  before{
    reset(TransferManagerMock)
  }

  describe("Given the 'S3Client', "){

    it("should fail when using a wrong s3Path"){
      val response = S3ClientMock.publish(SampleInvalidS3Uri, SampleDirectoryPath)
      response shouldBe Failure(S3InvalidProtocol)
    }

    it("should fail when using an invalid LocalPath"){
      val response = S3ClientMock.publish(SampleS3Uri, SampleInvalidFilePath)
      response shouldBe Failure(S3InvalidLocalPath)
    }

    it("I should get an error if something goes wrong"){
      val SampleFailureResult = new RuntimeException("Sample Exception")

        when(TransferManagerMock.uploadDirectory(
          mkEq(SampleS3Bucket),
          mkEq(SampleS3Key),
          mkEq(SampleDirectoryPath),
          anyBoolean()
        )).thenThrow(SampleFailureResult)

      val response = S3ClientMock.publish(SampleS3Uri, SampleDirectoryPath)
      response shouldBe Failure(SampleFailureResult)
    }

    it("I should be able to publish a directory"){
      val SampleSuccessResult = mock(classOf[MultipleFileUpload])

      when(TransferManagerMock.uploadDirectory(
        mkEq(SampleS3Bucket),
        mkEq(SampleS3Key),
        mkEq(SampleDirectoryPath),
        anyBoolean()
      ))
      .thenReturn(SampleSuccessResult)

      val response = S3ClientMock.publish(SampleS3Uri, SampleDirectoryPath)
      response shouldBe Success()
    }


    it("I should be able to publish a file"){
      val SampleSuccessResult = mock(classOf[Upload])

      when(TransferManagerMock.upload(
        mkEq(SampleS3Bucket),
        mkEq(SampleS3Key),
        mkEq(SampleFilePath)
      ))
      .thenReturn(SampleSuccessResult)

      val response = S3ClientMock.publish(SampleS3Uri, SampleFilePath)
      response shouldBe Success()
    }
  }
}

object S3ClientTest {

  val TransferManagerMock   = mock(classOf[TransferManager])

  val SampleS3Bucket        = "artifacts-repo"
  val SampleS3Key           = "test"
  val SampleS3Uri           = uri(s"s3://$SampleS3Bucket/$SampleS3Key")
  val SampleDirectoryPath   = file("./src/test/resources/cloudformation/templates")
  val SampleFilePath        = file("./src/test/resources/cloudformation/templates/template.json")
  val SampleInvalidS3Uri    = uri(s"http://$SampleS3Bucket/$SampleS3Key")
  val SampleInvalidFilePath = file("./invalid path")

  object S3ClientMock extends S3Client {
    val authCredentials: StackAuth = AuthProfile()
    val region:String              = "eu-west-1"

    override lazy val transferManager = TransferManagerMock
  }
}
