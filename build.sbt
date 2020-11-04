
import Dependencies._

coverageHighlighting := false

lazy val buildNumber = sys.env.get("BUILD_NUMBER").map(bn => s"b$bn")
lazy val CommonSettings = Seq(
  name              := "sbt-pipeline-plugin",
  organization      := "uk.co.telegraph",
  version           := "1.1.0-" + buildNumber.getOrElse("SNAPSHOT"),
  scalaVersion      := "2.10.6",
  isSnapshot        := buildNumber.isEmpty,
  logBuffered       := false,
  sbtPlugin         := true,
  scalacOptions     ++= Seq("-feature")
)

lazy val root = (project in file(".")).
  settings(CommonSettings: _*).
  settings(
    (parallelExecution in test) := false
  )

libraryDependencies ++= ProjectDependencies ++ UnitTestDependencies

publishTo := {
  if( isSnapshot.value ){
    Some("mvn-artifacts" atS3 "s3://mvn-artifacts/snapshot")
  }else {
    Some("mvn-artifacts" atS3 "s3://mvn-artifacts/release")
  }
}
