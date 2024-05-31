package online.walletstate.utils

import online.walletstate.models.AppError
import zio.*

object ZIOExtensions {
  
  extension [Env, T](zio: ZIO[Env, AppError, Option[T]])
    def getOrError[E <: AppError](error: E): ZIO[Env, E, T] = zio.some.mapError(_ => error)

  extension [Env, T](zio: ZIO[Env, AppError, Iterable[T]])
    def headOrError[E <: AppError](error: E): ZIO[Env, E, T] = zio.map(_.headOption).getOrError(error)

}
