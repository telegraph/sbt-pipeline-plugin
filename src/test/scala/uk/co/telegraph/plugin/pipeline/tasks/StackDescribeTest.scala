package uk.co.telegraph.plugin.pipeline.tasks

import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mEq}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import sbt.Logger
import uk.co.telegraph.cloud._
import uk.co.telegraph.plugin.pipeline.StackAuth
import StackDescribeTest._

@RunWith(classOf[JUnitRunner])
class StackDescribeTest extends FunSpec with BeforeAndAfter with Matchers{

  val SampleInvalidStackName = "invalid-test-stack"

  before{
    Mockito.reset(MockInterpreter)
  }

  describe("Given the 'StackDescribe' operation, "){
    it("I should be able to get a stack name if the stack exists"){
      Mockito.when(MockInterpreter.apply( mEq(Describe(SampleStackName)) ))
        .thenReturn(Some(SampleStackInfo))

      val res = StackDescribeMocked(
        SampleStackName,
        SampleStackRegion,
        SampleStackAuth
      )(SampleEnvironment, SampleLogger)

      res shouldBe Some(SampleStackInfo)

      verify(MockInterpreter, times(1)).apply( mEq(Describe(SampleStackName)) )
    }

    it("I should get nothing with an invalid stack"){
      Mockito.when(MockInterpreter.apply( mEq(Describe(SampleInvalidStackName)) ))
        .thenReturn(None)

      val res = StackDescribeMocked(
        SampleInvalidStackName,
        SampleStackRegion,
        SampleStackAuth
      )(SampleEnvironment, SampleLogger)

      res shouldBe None

      verify(MockInterpreter, times(1)).apply( mEq(Describe(SampleInvalidStackName)) )
    }
  }
}

object StackDescribeTest {
  object StackDescribeMocked extends StackDescribe{
    override def interpreter(region: StackRegion, auth: StackAuth)(implicit logger: Logger) = MockInterpreter
  }
}
