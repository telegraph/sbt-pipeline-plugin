package uk.co.telegraph.cloud

import cats.{Id, ~>}
import sbt.Logger

package object aws {

  private def impureInterpreter(region:StackRegion, auth:AuthCredentials)(implicit log:Logger):CloudInstruction ~> Id = new (CloudInstruction ~> Id){

    private lazy val s3Client = AwsS3Bucket      (region, auth)
    private lazy val cfClient = AwsCloudFormation(region, auth)

    override def apply[A](fa: CloudInstruction[A]): Id[A] = {
      fa match {
        case Describe(stackName) =>
          cfClient.describe(stackName).asInstanceOf[A]
        case Create(stackName, stackConfig) =>
          cfClient.create(stackName, stackConfig).asInstanceOf[A]
        case Update(stackName, stackConfig) =>
          cfClient.update(stackName, stackConfig).asInstanceOf[A]
        case Delete(stackName) =>
          cfClient.delete(stackName).asInstanceOf[A]
        case Await(stackName) =>
          cfClient.await(stackName).asInstanceOf[A]
        case PushTemplate(storageUrl, localPath) =>
          s3Client.pushTemplate(storageUrl, localPath).asInstanceOf[A]
      }
    }
  }

  def interpreter(region:StackRegion, auth:AuthCredentials)(implicit log:Logger): ~>[CloudInstruction, Id] =
    impureInterpreter(region, auth)
}
