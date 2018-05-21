
import sbt._

object Dependencies {

  private lazy val json4sVersion  = "3.5.4"
  private lazy val awsSdkVersion  = "1.11.103"

  lazy val ProjectDependencies = Seq(
    "com.amazonaws" % "aws-java-sdk-cloudformation" % awsSdkVersion,
    "com.amazonaws" % "aws-java-sdk-s3"             % awsSdkVersion,
    "com.amazonaws" % "aws-java-sdk-kms"            % awsSdkVersion,
    "com.amazonaws" % "aws-java-sdk-core"           % awsSdkVersion,
    "com.amazonaws" % "jmespath-java"               % awsSdkVersion,

    "org.json4s"    %% "json4s-native" % json4sVersion,
    "org.typelevel" %% "cats-free"     % "0.9.0"
  )

  lazy val UnitTestDependencies = Seq(
    "junit"         %  "junit"       % "4.12"  % Test,
    "org.mockito"   % "mockito-core" % "2.7.2" % Test,
    "org.scalatest" %% "scalatest"   % "3.0.1" % Test
  )
}
