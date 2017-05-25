package uk.co.telegraph.cloud.aws

import com.amazonaws.auth.AWSCredentialsProvider
import sbt.Logger
import uk.co.telegraph.cloud.AuthCredentials

private [aws] trait AwsClientWithAuth {

  val authCredentials: AuthCredentials
  val region         : String
  val log            : Logger

  import auth._

  def authProvider: AWSCredentialsProvider = authCredentials.toProvider
}
