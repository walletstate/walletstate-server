package online.walletstate.utils

import online.walletstate.models.AppError
import zio.*

object ZIOExtensions {

  extension [T](task: Task[Option[T]])
    def getOrError[E <: AppError](error: E): Task[T] =
      task.flatMap {
        case Some(value) => ZIO.succeed(value)
        case None        => ZIO.fail(error)
      }

  extension [T](task: Task[Iterable[T]])
    def headOrError[E <: AppError](error: E): Task[T] = task.map(_.headOption).getOrError(error)

}
