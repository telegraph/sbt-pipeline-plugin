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
class StackUpdateTest extends FunSpec with BeforeAndAfter with Matchers{

  import StackUpdateTest._

  before{
    Mockito.reset(MockInterpreter)
  }

  describe("Given the 'StackUpdate' operation, "){
    it("I should be able to update an existing Stack"){
      Mockito.when(MockInterpreter.apply( mEq(Update(SampleStackName, ExpectedStackConfig)) ))
        .thenReturn(())
      when(MockInterpreter.apply( mEq(Await(SampleStackName)) ))
        .thenReturn(Some("UPDATE_COMPLETE"))

      val res = StackUpdateMocked(
        SampleStackName,
        SampleStackRegion,
        SampleStackAuth,
        SampleStackCapabilities,
        SampleStackTags,
        SampleParamPath,
        SampleParamCustom,
        SampleTemplateS3Uri
      )(SampleEnvironment, SampleLogger)

      res shouldBe ()

      verify(MockInterpreter, times(1)).apply( mEq(Update(SampleStackName, ExpectedStackConfig)) )
      verify(MockInterpreter, times(1)).apply( mEq(Await(SampleStackName )) )
    }

    it("I should get an exception if stack rolls back"){
      Mockito.when(MockInterpreter.apply( mEq(Update(SampleStackName, ExpectedStackConfig)) ))
        .thenReturn(())
      when(MockInterpreter.apply( mEq(Await(SampleStackName)) ))
        .thenReturn(Some("ROLLBACK_COMPLETED"))

      val ex = intercept[RuntimeException]{
        StackUpdateMocked(
          SampleStackName,
          SampleStackRegion,
          SampleStackAuth,
          SampleStackCapabilities,
          SampleStackTags,
          SampleParamPath,
          SampleParamCustom,
          SampleTemplateS3Uri
        )(SampleEnvironment, SampleLogger)
      }

      ex.getMessage shouldBe "ERROR: Stack Update failed - 'ROLLBACK_COMPLETED'"
      verify(MockInterpreter, times(1)).apply( mEq(Update(SampleStackName, ExpectedStackConfig)) )
      verify(MockInterpreter, times(1)).apply( mEq(Await (SampleStackName )) )
    }


    it("I should get an exception if an exception is thrown during StackCreate"){
      Mockito.when(MockInterpreter.apply( mEq(Update(SampleStackName, ExpectedStackConfig)) ))
        .thenThrow(new RuntimeException("Simple Failure"))

      val ex = intercept[RuntimeException]{
        StackUpdateMocked(
          SampleStackName,
          SampleStackRegion,
          SampleStackAuth,
          SampleStackCapabilities,
          SampleStackTags,
          SampleParamPath,
          SampleParamCustom,
          SampleTemplateS3Uri
        )(SampleEnvironment, SampleLogger)
      }

      ex.getMessage shouldBe "ERROR: Fail during 'stackUpdate' - Simple Failure"
      verify(MockInterpreter, times(1)).apply( mEq(Update(SampleStackName, ExpectedStackConfig)) )
      verify(MockInterpreter, times(0)).apply( mEq(Await (SampleStackName )) )
    }
  }
}

object StackUpdateTest{

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

  object StackUpdateMocked extends StackUpdate{
    override def interpreter(region: StackRegion, auth: StackAuth)(implicit logger: Logger) = MockInterpreter
  }
}
