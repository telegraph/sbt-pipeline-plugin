package uk.co.telegraph.plugin.pipeline

import cats.{Id, ~>}
import com.amazonaws.services.cloudformation.model.Stack
import org.mockito.Mockito.mock
import sbt._
import uk.co.telegraph.cloud.{AuthProfile, CloudInstruction, JsonFormat}

package object tasks {

  val SampleStackName         : String = "test-stack"
  val SampleStackRegion       : String = "eu-west-1"
  val SampleStackAuth         : StackAuth = AuthProfile()
  val SampleStackCapabilities : Seq[String] = Seq("CAPABILITY_IAM")
  val SampleStackTags         : Map[String, String] = Map("Billing" -> "Platforms")
  val SampleParamPath         : File = file("params.file")
  val SampleParamCustom       : StackParams = Map.empty
  val SampleTemplateS3Uri     : URI = uri("s3://sample-bucket")
  val SampleEnvironment       : String = "static"
  val SampleLogger            : Logger = mock(classOf[Logger])
  val SampleStackStatus       : String = "COMPLETE"
  val SampleTemplateFormat    : StackTemplateFormat = JsonFormat
  val SampleStackInfo         : Stack = new Stack()
    .withStackName(SampleStackName)

  val MockInterpreter: ~>[CloudInstruction, Id] = mock(classOf[CloudInstruction ~> Id])


}
