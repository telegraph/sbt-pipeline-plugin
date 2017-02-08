package uk.co.telegraph.stack.auth

import com.amazonaws.auth.AWSCredentialsProvider

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