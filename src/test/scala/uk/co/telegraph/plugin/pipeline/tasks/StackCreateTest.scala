package uk.co.telegraph.plugin.pipeline.tasks

import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mEq}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import sbt.{Logger, _}
import uk.co.telegraph.cloud._
import uk.co.telegraph.plugin.pipeline.StackAuth

@RunWith(classOf[JUnitRunner])
class StackCreateTest extends FunSpec with BeforeAndAfter with Matchers{

  import StackCreateTest._

  before{
    Mockito.reset(MockInterpreter)
  }

  describe("Given the 'StackDescribe' operation, "){
    it("I should be able to get a stack name if the stack exists"){
      Mockito.when(MockInterpreter.apply( mEq(Create(SampleStackName, ExpectedStackConfig)) ))
        .thenReturn(())
      when(MockInterpreter.apply( mEq(Await(SampleStackName)) ))
        .thenReturn(Some("CREATE_COMPLETE"))

      val res = StackCreateMocked(
        SampleStackName,
        SampleStackRegion,
        SampleStackAuth,
        SampleStackCapabilities,
        SampleStackTags,
        SampleParamPath,
        SampleParamCustom,
        SampleTemplateS3Uri,
        SampleTemplateFormat
      )(SampleEnvironment, SampleLogger)

      res shouldBe ()

      verify(MockInterpreter, times(1)).apply( mEq(Create(SampleStackName, ExpectedStackConfig)) )
      verify(MockInterpreter, times(1)).apply( mEq(Await(SampleStackName )) )
    }

    it("I should get an exception if stack rolls back"){
      Mockito.when(MockInterpreter.apply( mEq(Create(SampleStackName, ExpectedStackConfig)) ))
        .thenReturn(())
      when(MockInterpreter.apply( mEq(Await(SampleStackName)) ))
        .thenReturn(Some("ROLLBACK_COMPLETED"))

      val ex = intercept[RuntimeException]{
        StackCreateMocked(
          SampleStackName,
          SampleStackRegion,
          SampleStackAuth,
          SampleStackCapabilities,
          SampleStackTags,
          SampleParamPath,
          SampleParamCustom,
          SampleTemplateS3Uri,
          SampleTemplateFormat
        )(SampleEnvironment, SampleLogger)
      }

      ex.getMessage shouldBe "ERROR: Stack Deployment failed - 'ROLLBACK_COMPLETED'"
      verify(MockInterpreter, times(1)).apply( mEq(Create(SampleStackName, ExpectedStackConfig)) )
      verify(MockInterpreter, times(1)).apply( mEq(Await (SampleStackName )) )
    }


    it("I should get an exception if an exception is thrown during StackCreate"){
      Mockito.when(MockInterpreter.apply( mEq(Create(SampleStackName, ExpectedStackConfig)) ))
        .thenThrow(new RuntimeException("Simple Failure"))

      val ex = intercept[RuntimeException]{
        StackCreateMocked(
          SampleStackName,
          SampleStackRegion,
          SampleStackAuth,
          SampleStackCapabilities,
          SampleStackTags,
          SampleParamPath,
          SampleParamCustom,
          SampleTemplateS3Uri,
          SampleTemplateFormat
        )(SampleEnvironment, SampleLogger)
      }

      ex.getMessage shouldBe "ERROR: Fail during 'stackCreate' - Simple Failure"
      verify(MockInterpreter, times(1)).apply( mEq(Create(SampleStackName, ExpectedStackConfig)) )
      verify(MockInterpreter, times(0)).apply( mEq(Await (SampleStackName )) )
    }
  }
}

object StackCreateTest {
  val SampleStackConfig = StackConfig(
    SampleStackCapabilities,
    SampleTemplateS3Uri,
    SampleStackTags,
    SampleParamCustom
  )
  val ExpectedStackConfig = StackConfig(
    SampleStackCapabilities,
    uri(s"${SampleTemplateS3Uri.toString}/template.json"),
    SampleStackTags,
    SampleParamCustom
  )
  object StackCreateMocked extends StackCreate{
    override def interpreter(region: StackRegion, auth: StackAuth)(implicit logger: Logger) = MockInterpreter
  }
}
