package uk.co.telegraph.plugin.pipeline

import cats.{Id, ~>}
import com.amazonaws.services.cloudformation.model.Stack
import org.mockito.Mockito.mock
import sbt._
import uk.co.telegraph.cloud.{AuthProfile, CloudInstruction}

package object tasks {

  val SampleStackName = "test-stack"
  val SampleStackRegion = "eu-west-1"
  val SampleStackAuth = AuthProfile()
  val SampleStackCapabilities = Seq("CAPABILITY_IAM")
  val SampleStackTags = Map("Billing" -> "Platforms")
  val SampleParamPath = file("params.file")
  val SampleParamCustom:StackParams = Map.empty
  val SampleTemplateS3Uri = uri("s3://sample-bucket")
  val SampleEnvironment = "static"
  val SampleLogger = mock(classOf[Logger])
  val SampleStackStatus = "COMPLETE"

  val SampleStackInfo = new Stack()
    .withStackName(SampleStackName)

  val MockInterpreter = mock(classOf[CloudInstruction ~> Id])


}
