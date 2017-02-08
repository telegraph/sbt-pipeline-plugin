package uk.co.telegraph.stack

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import sbt._
import uk.co.telegraph.stack.S3Client._
import uk.co.telegraph.stack.auth.AuthCredentials

import scala.util.{Failure, Try}

trait S3Client extends ClientWithAuth {

  private [stack] lazy val s3Client = AmazonS3ClientBuilder.standard()
    .withCredentials(authProvider)
    .withRegion     (region)
    .build()

  private [stack] lazy val transferManager = TransferManagerBuilder.standard()
    .withS3Client(s3Client)
    .build()

  /**
   * Method used to publish a directory
   */
  def publish( s3Path:URI, localPath:File ):Try[Unit] = {
    if( s3Path.getScheme != "s3" ){
      return Failure(S3InvalidProtocol)
    }
    if( !localPath.exists() ){
      return Failure(S3InvalidLocalPath)
    }

    val s3Bucket = s3Path.getHost
    val s3Key    = s3Path.getPath.replaceFirst("^/", "")
    Try(localPath.isDirectory).map({
        case true  =>
          transferManager.uploadDirectory(s3Bucket, s3Key, localPath, true).waitForCompletion()
        case false =>
          transferManager.upload         (s3Bucket, s3Key, localPath      ).waitForCompletion()
    })
  }
}



object S3Client {

  private case class S3ClientImp(authCredentials: AuthCredentials, region:String) extends S3Client

  object S3InvalidProtocol extends Exception{
    override def getMessage: String = "S3Client - Invalid protocol for S3 Path (s3://{s3Bucket}/{s3Key})."
  }

  object S3InvalidLocalPath extends Exception {
    override def getMessage: String = "S3Client - Invalid LocalPath - Path does not exist."
  }

  def apply( authCredentials:AuthCredentials, region:String):S3Client = {
    S3ClientImp(authCredentials,region)
  }
}