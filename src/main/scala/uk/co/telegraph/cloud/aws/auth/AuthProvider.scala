package uk.co.telegraph.cloud.aws.auth

import com.amazonaws.auth.AWSCredentialsProvider
import uk.co.telegraph.cloud.AuthCredentials

trait AuthProvider[A <: AuthCredentials]{
  def authenticate(authentication:A):AWSCredentialsProvider
}

object AuthProvider{
  def apply[A <: AuthCredentials:AuthProvider]:AuthProvider[A] = implicitly
}