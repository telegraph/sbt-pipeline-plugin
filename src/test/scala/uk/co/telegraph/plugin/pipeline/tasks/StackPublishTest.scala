package uk.co.telegraph.plugin.pipeline.tasks

import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mEq}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import sbt.Logger
import sbt._
import uk.co.telegraph.cloud._
import uk.co.telegraph.plugin.pipeline.StackAuth

@RunWith(classOf[JUnitRunner])
class StackPublishTest extends FunSpec with BeforeAndAfter with Matchers{

  import StackPublishTest._

  before{
    Mockito.reset(MockInterpreter)
  }

  describe("Given the 'StackPublish' operation, "){
    it("I should be able to publish files"){
      Mockito.when(MockInterpreter.apply( mEq(PushTemplate(SampleStackTempS3Uri, SampleStackTempPath)) ))
        .thenReturn(())

      val res = StackPublishMocked(
        SampleStackRegion,
        SampleStackAuth,
        SampleStackTempPath,
        SampleStackTempS3Uri
      )(SampleEnvironment, SampleLogger)

      res shouldBe SampleStackTempS3Uri

      verify(MockInterpreter, times(1)).apply( mEq(PushTemplate(SampleStackTempS3Uri, SampleStackTempPath)) )
    }

    it("I should get an error if an exception is fired"){
      Mockito.when(MockInterpreter.apply( mEq(PushTemplate(SampleStackTempS3Uri, SampleStackTempPath)) ))
        .thenThrow(new RuntimeException("test exception"))

      val res = intercept[RuntimeException](
        StackPublishMocked(
          SampleStackRegion,
          SampleStackAuth,
          SampleStackTempPath,
          SampleStackTempS3Uri
        )(SampleEnvironment, SampleLogger)
      )
      res.getMessage shouldBe "ERROR: Fail during 'stackPublish' - test exception"

      verify(MockInterpreter, times(1)).apply( mEq(PushTemplate(SampleStackTempS3Uri, SampleStackTempPath)) )
    }
  }
}

object StackPublishTest {
  val SampleStackTempPath  = file("test")
  val SampleStackTempS3Uri = uri("test")

  object StackPublishMocked extends StackPublish{
    override def interpreter(region: StackRegion, auth: StackAuth)(implicit logger: Logger) = MockInterpreter
  }
}