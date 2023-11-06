package online.walletstate.utils

import online.walletstate.models.errors.AppError
import zio.*

object ZIOExtensions {

  extension [T](task: Task[Option[T]])
    def getOrError[E <: AppError](error: E): Task[T] =
      task.flatMap {
        case Some(value) => ZIO.succeed(value)
        case None        => ZIO.fail(error)
      }

}
