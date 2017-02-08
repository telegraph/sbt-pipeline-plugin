
import Dependencies._

lazy val buildNumber = sys.env.get("BUILD_NUMBER").map( bn => s"b$bn")
lazy val CommonSettings = Seq(
  name              := "sbt-pipeline-plugin",
  organization      := "uk.co.telegraph",
  version           := "1.0.0-" + buildNumber.getOrElse("dev"),
  scalaVersion      := "2.10.6",
  isSnapshot        := buildNumber.isEmpty,
  sbtPlugin         := true
)

lazy val root = (project in file(".")).
  settings(CommonSettings: _*)

libraryDependencies ++= ProjectDependencies ++ UnitTestDependencies

publishTo := Some("mvn-artifacts" at "s3://mvn-artifacts/release")