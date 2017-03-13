
import com.amazonaws.services.cloudformation.model.Stack

val checkStackCreated = TaskKey[Unit]("check-stack-created")
val checkStackDeleted = TaskKey[Unit]("check-stack-deleted")
val checkStackUpdated = TaskKey[Unit]("check-stack-updated")
val prepareCreate     = TaskKey[Unit]("prepare-create")
val prepareUpdate     = TaskKey[Unit]("prepare-update")
lazy val testFullRun = (project in file(".")).
  configs (IntegrationTest).
  settings(Defaults.itSettings: _*).
  settings(
    version         := "01",
    scalaVersion    := "2.10.6",
    prepareCreate :={
      IO.move( baseDirectory.value / "resources" / "create" / "parameters-static.json", baseDirectory.value / "infrastructure" / "static" / "parameters" / "parameters-static.json")
    },
    prepareUpdate :={
      IO.move( baseDirectory.value / "resources" / "update" / "parameters-static.json", baseDirectory.value / "infrastructure" / "static" / "parameters" / "parameters-static.json")
    },
    checkStackCreated := {
      val result:Option[Stack] = (stackDescribe in DeployStatic).value
      result.map(_.getStackStatus) match {
        case Some(status) if status == "CREATE_COMPLETE" =>
          streams.value.log.info("SUCCESS")
        case Some(status) =>
          sys.error(s"Invalid Status '$status'")
        case None =>
          sys.error("Fail to get Stack Status")
      }
    },
    checkStackUpdated := {
      val result:Option[Stack] = (stackDescribe in DeployStatic).value

      result.map(_.getStackStatus) match {
        case Some(status) if status == "UPDATE_COMPLETE" =>
          streams.value.log.info("SUCCESS")
        case Some(status) =>
          sys.error(s"Invalid Status '$status'")
        case None =>
          sys.error("Fail to get Stack Status")
      }
    },
    checkStackDeleted := {
      val result:Option[Stack] = (stackDescribe in DeployStatic).value

      result.map(_.getStackStatus) match {
        case Some(status) =>
          sys.error(s"Stack should not exist '$status'")
        case None =>
          streams.value.log.info("SUCCESS")
      }
    }
  )

libraryDependencies += "com.amazonaws" % "aws-java-sdk-cloudformation" % "1.11.103"
