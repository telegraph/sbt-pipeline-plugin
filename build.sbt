
import Dependencies._

lazy val buildNumber = sys.env.get("BUILD_NUMBER")
lazy val CommonSettings = Seq(
  name              := "sbt-pipeline-plugin",
  organization      := "uk.co.telegraph",
  version           := "1.1.0." + buildNumber.getOrElse("dev"),
  scalaVersion      := "2.10.6",
  isSnapshot        := buildNumber.isEmpty,
  sbtPlugin         := true,
  publishMavenStyle := false
)

lazy val root = (project in file(".")).
  settings(CommonSettings: _*)

libraryDependencies ++= ProjectDependencies ++ UnitTestDependencies

publishTo := {
  if( isSnapshot.value ){
    Some("mvn-artifacts" at "s3://mvn-artifacts/snapshot")
  }else {
    Some("mvn-artifacts" at "s3://mvn-artifacts/release")
  }
}