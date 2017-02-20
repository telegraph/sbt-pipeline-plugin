package uk.co.telegraph.cloud.aws

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProvider, AWSStaticCredentialsProvider, BasicAWSCredentials, EnvironmentVariableCredentialsProvider}
import uk.co.telegraph.cloud.{AuthCredentials, AuthEnvVars, AuthProfile, AuthToken}

/**
 * Created: rodriguesa 
 * Date   : 03/02/2017
 * Project: sbt-pipeline-plugin
 */
package object auth {

  implicit object AuthTokenProvider extends AuthProvider[AuthToken]{
    override def authenticate(authToken: AuthToken): AWSCredentialsProvider = {
      val basicAuth = new BasicAWSCredentials(authToken.accessToken, authToken.secretToken)
      new AWSStaticCredentialsProvider(basicAuth)
    }
  }

  implicit object AuthProfileProvider extends AuthProvider[AuthProfile]{
    override def authenticate(authProfile: AuthProfile): AWSCredentialsProvider = {
      new ProfileCredentialsProvider(authProfile.profileName.orNull)
    }
  }

  implicit object AuthEnvVarsProvider extends AuthProvider[AuthEnvVars]{
    override def authenticate(x: AuthEnvVars): AWSCredentialsProvider = {
      new EnvironmentVariableCredentialsProvider()
    }
  }

  private def doAuthenticate[A <: AuthCredentials:AuthProvider](thing:A) = AuthProvider[A].authenticate(thing)

  implicit class AuthenticationOper[A <: AuthCredentials]( authentication: A){
    def toProvider: AWSCredentialsProvider = {
      //TODO: There must be a way to remove this thing!
      authentication match {
        case auth:AuthProfile => doAuthenticate( auth )
        case auth:AuthToken   => doAuthenticate( auth )
        case auth:AuthEnvVars => doAuthenticate( auth )
      }
    }
  }
}
