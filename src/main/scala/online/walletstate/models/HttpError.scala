package online.walletstate.models

import zio.http.Header.Accept.MediaTypeWithQFactor
import zio.http.codec.HttpCodec
import zio.http.{Response, Status}
import zio.schema.{Schema, derived}
import zio.{NonEmptyChunk, UIO, ZIO}

import scala.reflect.ClassTag

sealed trait HttpError[T <: HttpError[T]: Schema](status: Status) {
  val error: String
  val message: String

  def encode(mediaType: NonEmptyChunk[MediaTypeWithQFactor]): Response =
    HttpCodec.error[T](status).encodeResponse(this.asInstanceOf[T], mediaType)

  def encodeZIO(mediaType: NonEmptyChunk[MediaTypeWithQFactor]): UIO[Response] =
    ZIO.succeed(encode(mediaType))
}

object HttpError {

  final case class BadRequest(error: String, message: String) extends HttpError[BadRequest](BadRequest.status)
      derives Schema
  object BadRequest {
    final val status: Status                  = Status.BadRequest
    def apply(appError: AppError): BadRequest = BadRequest(appError.errorName, appError.message)
  }

  final case class Unauthorized(error: String, message: String) extends HttpError[Unauthorized](Unauthorized.status)
      derives Schema
  object Unauthorized {
    final val status: Status                    = Status.Unauthorized
    def apply(appError: AppError): Unauthorized = Unauthorized(appError.errorName, appError.message)
  }

  final case class Forbidden(error: String, message: String) extends HttpError[Forbidden](Forbidden.status)
      derives Schema
  object Forbidden {
    final val status: Status                 = Status.Forbidden
    def apply(appError: AppError): Forbidden = Forbidden(appError.errorName, appError.message)
  }

  final case class NotFound(error: String, message: String) extends HttpError[NotFound](NotFound.status) derives Schema
  object NotFound {
    final val status: Status                = Status.NotFound
    def apply(appError: AppError): NotFound = NotFound(appError.errorName, appError.message)
  }

  final case class Conflict(error: String, message: String) extends HttpError[Conflict](Conflict.status) derives Schema
  object Conflict {
    final val status: Status                = Status.Conflict
    def apply(appError: AppError): Conflict = Conflict(appError.errorName, appError.message)
  }

  final case class InternalServerError(error: String, message: String)
      extends HttpError[InternalServerError](InternalServerError.status) derives Schema
  object InternalServerError {
    final val status: Status                           = Status.InternalServerError
    def apply(message: String): InternalServerError    = InternalServerError("InternalServerError", message)
    def apply(appError: AppError): InternalServerError = InternalServerError(appError.errorName, appError.message)
    val default: InternalServerError                   = InternalServerError("Oops.... Something went wrong")
  }
}
