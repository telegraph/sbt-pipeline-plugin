package uk.co.telegraph

import cats.free.Free
import cats.free.Free.liftF
import com.amazonaws.services.cloudformation.model.Stack
import sbt._
import uk.co.telegraph.plugin.pipeline.{StackParams, StackTags}

package object cloud {

  type StackName   = String
  type StackStatus = String
  type StackRegion = String

  sealed trait AuthCredentials

  case class AuthToken  (accessToken:String, secretToken:String) extends AuthCredentials
  case class AuthProfile(profileName:Option[String]=None) extends AuthCredentials
  case class AuthEnvVars() extends AuthCredentials

  case class StackConfig(capabilities:Seq[String], templateUri:URI, tags:StackTags, parameters:StackParams)

  sealed trait CloudInstruction[A]
  final case class Describe    (name: StackName) extends CloudInstruction[Option[Stack]]
  final case class Await       (name: StackName) extends CloudInstruction[Option[StackStatus]]
  final case class Delete      (name: StackName) extends CloudInstruction[Unit]
  final case class Create      (name: StackName, config:StackConfig) extends CloudInstruction[Unit]
  final case class Update      (name: StackName, config:StackConfig) extends CloudInstruction[Unit]

  final case class PushTemplate(storageUrl: URI, localPath:File) extends CloudInstruction[Unit]

  object dls {
    type CloudInst[A] = Free[CloudInstruction, A]

    def describe(name: StackName): CloudInst[Option[Stack]] =
      liftF(Describe(name))

    def delete(name: StackName): CloudInst[Unit] =
      liftF(Delete(name))

    def create(name: StackName, config: StackConfig): CloudInst[Unit] =
      liftF(Create(name, config))

    def update(name: StackName, config: StackConfig): CloudInst[Unit] =
      liftF(Update(name, config))

    def createOrUpdate(name:StackName, config:StackConfig): CloudInst[Unit] = {
      describe(name).flatMap({
        case Some(_) => update(name, config)
        case None    => create(name, config)
      })
    }

    def status(name:StackName):CloudInst[Option[StackStatus]] = {
      describe(name).map( stack => stack.map(_.getStackStatus) )
    }

    def await(name:StackName):CloudInst[Option[StackStatus]] = {
      liftF(Await(name))
    }

    def pushTemplate( storageUrl:URI, localPath:File ): CloudInst[Unit] = {
      liftF( PushTemplate(storageUrl, localPath) )
    }
  }
}
