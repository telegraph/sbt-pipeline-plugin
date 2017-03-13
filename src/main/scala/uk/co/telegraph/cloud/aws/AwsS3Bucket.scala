package uk.co.telegraph.cloud.aws

import com.amazonaws.AmazonWebServiceRequest
import com.amazonaws.event.{ProgressEvent, ProgressEventType, SyncProgressListener}
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ProgressEvent
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import com.amazonaws.services.s3.transfer.internal.TransferProgressUpdatingListener
import sbt.{File, Logger, URI}
import uk.co.telegraph.cloud.AuthCredentials

private [aws] trait AwsS3Bucket { this: AwsClientWithAuth =>

  import AwsS3Bucket._

  lazy val s3Client = AmazonS3ClientBuilder.standard()
    .withRegion     ( region )
    .withCredentials( authProvider )
    .build()

  lazy val transferManager = TransferManagerBuilder.standard()
    .withS3Client(s3Client)
    .build()

  /**
    * Pushes Templates to S3 Bucket
    */
  def pushTemplate(storageUrl:URI, localPath:File):Unit = {
    if( storageUrl.getScheme != "s3" ){
      throw S3InvalidProtocol
    }
    if( !localPath.exists() ){
      throw S3InvalidLocalPath
    }

    val s3Bucket = storageUrl.getHost
    val s3Key    = storageUrl.getPath.replaceFirst("^/", "")

    if (localPath.isDirectory) {
      transferManager.uploadDirectory(s3Bucket, s3Key, localPath, true).waitForCompletion()
    } else {
      transferManager.upload(s3Bucket, s3Key, localPath).waitForCompletion()
    }
  }
}

object AwsS3Bucket {
  object S3InvalidProtocol extends Exception{
    override def getMessage: String = "S3Client - Invalid protocol for S3 Path (s3://{s3Bucket}/{s3Key})."
  }

  object S3InvalidLocalPath extends Exception {
    override def getMessage: String = "S3Client - Invalid LocalPath - Path does not exist."
  }

  private case class AwsS3BucketImp(region:String, authCredentials: AuthCredentials, log: Logger)
    extends AwsS3Bucket with AwsClientWithAuth

  def apply(region:String, authCredentials: AuthCredentials)(implicit log:Logger):AwsS3Bucket = {
    AwsS3BucketImp(region, authCredentials, log)
  }
}