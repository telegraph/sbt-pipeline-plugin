package uk.co.telegraph.plugin.pipeline.tasks

import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.{eq => mEq}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}
import sbt.Logger
import uk.co.telegraph.cloud._
import uk.co.telegraph.plugin.pipeline.StackAuth

@RunWith(classOf[JUnitRunner])
class StackDeleteTest extends FunSpec with BeforeAndAfter with Matchers{

  import StackDeleteTest._

  before{
    Mockito.reset(MockInterpreter)
  }

  describe("Given the 'StackDescribe' operation, "){
    it("I should be able to get a stack name if the stack exists"){
      when(MockInterpreter.apply( mEq(Delete(SampleStackName)) ))
        .thenReturn(())
      when(MockInterpreter.apply( mEq(Await(SampleStackName)) ))
        .thenReturn(Some(SampleStackStatus))

      val res = StackDeleteMocked(
        SampleStackName,
        SampleStackRegion,
        SampleStackAuth
      )(SampleEnvironment, SampleLogger)

      res shouldBe ()

      verify(MockInterpreter, times(1)).apply( mEq(Delete(SampleStackName)) )
      verify(MockInterpreter, times(1)).apply( mEq(Await(SampleStackName )) )
    }

    it("Exceptions should not be propagated"){
      when(MockInterpreter.apply( mEq(Delete(SampleStackName)) ))
        .thenThrow( new RuntimeException("Failed Exception"))

      val res = StackDeleteMocked(
        SampleStackName,
        SampleStackRegion,
        SampleStackAuth
      )(SampleEnvironment, SampleLogger)

      res shouldBe ()

      verify(MockInterpreter, times(1)).apply( mEq(Delete(SampleStackName)) )
      verify(MockInterpreter, never() ).apply( mEq(Await(SampleStackName )) )
    }
  }
}

object StackDeleteTest {
  object StackDeleteMocked extends StackDelete{
    override def interpreter(region: StackRegion, auth: StackAuth)(implicit logger: Logger) = MockInterpreter
  }
}