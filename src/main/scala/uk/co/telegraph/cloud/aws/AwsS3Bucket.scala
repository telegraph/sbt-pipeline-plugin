package uk.co.telegraph.cloud.aws

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import sbt.{File, Logger, URI}
import uk.co.telegraph.cloud.AuthCredentials

import scala.util.{Failure, Try}

/**
  * Created: rodriguesa 
  * Date   : 17/02/2017
  * Project: Default (Template) Project
  */
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
  def pushTemplate(storageUrl:URI, localPath:File):Try[Unit] = {
    if( storageUrl.getScheme != "s3" ){
      return Failure(S3InvalidProtocol)
    }
    if( !localPath.exists() ){
      return Failure(S3InvalidLocalPath)
    }

    val s3Bucket = storageUrl.getHost
    val s3Key    = storageUrl.getPath.replaceFirst("^/", "")
    Try(localPath.isDirectory).map({
      case true  =>
        transferManager.uploadDirectory(s3Bucket, s3Key, localPath, true).waitForCompletion()
      case false =>
        transferManager.upload         (s3Bucket, s3Key, localPath      ).waitForCompletion()
    })
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