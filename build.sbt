
import Dependencies._

import scala.language.postfixOps

lazy val buildNumber = sys.env.get("BUILD_NUMBER").map(bn => s"b$bn")
lazy val CommonSettings = Seq(
  name              := "sbt-pipeline-plugin",
  organization      := "uk.co.telegraph",
  version           := "1.1.0-" + buildNumber.getOrElse("dev"),
  scalaVersion      := "2.10.6",
  isSnapshot        := buildNumber.isEmpty,
  sbtPlugin         := true,
  publishMavenStyle := false
)

lazy val root = (project in file(".")).
  settings(CommonSettings: _*).
  settings(
    concurrentRestrictions := Seq(
      Tags.limit(Tags.Test, 1)
    )
  )

libraryDependencies ++= ProjectDependencies ++ UnitTestDependencies

resolvers += "mvn-artifacts" atS3 "s3://mvn-artifacts/release"
publishTo := {
  if( isSnapshot.value ){
    Some("mvn-artifacts" at "s3://mvn-artifacts/snapshot")
  }else {
    Some("mvn-artifacts" at "s3://mvn-artifacts/release")
  }
}