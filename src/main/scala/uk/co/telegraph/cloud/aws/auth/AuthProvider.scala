package uk.co.telegraph.cloud.aws.auth

import com.amazonaws.auth.AWSCredentialsProvider
import uk.co.telegraph.cloud.AuthCredentials

/**
 * Created: rodriguesa 
 * Date   : 07/02/2017
 * Project: sbt-pipeline-plugin
 */
trait AuthProvider[A <: AuthCredentials]{
  def authenticate(authentication:A):AWSCredentialsProvider
}

object AuthProvider{
  def apply[A <: AuthCredentials:AuthProvider]:AuthProvider[A] = implicitly
}