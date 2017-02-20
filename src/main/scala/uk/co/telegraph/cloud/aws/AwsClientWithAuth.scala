package uk.co.telegraph.cloud.aws

import com.amazonaws.auth.AWSCredentialsProvider
import sbt.Logger
import uk.co.telegraph.cloud.AuthCredentials

/**
  * Created: rodriguesa 
  * Date   : 17/02/2017
  * Project: Default (Template) Project
  */
private [aws] trait AwsClientWithAuth {

  val authCredentials: AuthCredentials
  val region         : String
  val log            : Logger

  import auth._

  def authProvider: AWSCredentialsProvider = authCredentials.toProvider
}
