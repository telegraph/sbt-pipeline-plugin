package uk.co.telegraph.stack

import uk.co.telegraph.stack.auth._
import scala.language.{implicitConversions, postfixOps}

trait ClientWithAuth{
  val authCredentials: AuthCredentials
  val region         : String

  import auth._

  def authProvider = authCredentials.toProvider
}
