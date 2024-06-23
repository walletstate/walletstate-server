package online.walletstate.common.models

import zio.http.Header.Accept.MediaTypeWithQFactor
import zio.http.codec.HttpCodecType.Content
import zio.http.codec.{HttpCodec, HttpCodecType}
import zio.http.{Response, Status}
import zio.schema.{Schema, derived}
import zio.{NonEmptyChunk, UIO, ZIO}

sealed trait HttpError[T <: HttpError[T]: Schema](status: Status) {
  val error: String
  val message: String

  def encode(mediaType: NonEmptyChunk[MediaTypeWithQFactor]): Response =
    HttpCodec.error[T](status).encodeResponse(this.asInstanceOf[T], mediaType)

  def encodeZIO(mediaType: NonEmptyChunk[MediaTypeWithQFactor]): UIO[Response] =
    ZIO.succeed(encode(mediaType))
    
  val codec: HttpCodec[HttpCodecType.Status with Content, T] = HttpCodec.error[T](status)
}

object HttpError {

  final case class BadRequest(error: String, message: String) extends HttpError[BadRequest](BadRequest.status)
      derives Schema
  object BadRequest {
    final val status: Status = Status.BadRequest
  }

  final case class Unauthorized(error: String, message: String) extends HttpError[Unauthorized](Unauthorized.status)
      derives Schema
  object Unauthorized {
    final val status: Status = Status.Unauthorized
  }

  final case class Forbidden(error: String, message: String) extends HttpError[Forbidden](Forbidden.status)
      derives Schema
  object Forbidden {
    final val status: Status = Status.Forbidden
  }

  final case class NotFound(error: String, message: String) extends HttpError[NotFound](NotFound.status) derives Schema
  object NotFound {
    final val status: Status = Status.NotFound
  }

  final case class Conflict(error: String, message: String) extends HttpError[Conflict](Conflict.status) derives Schema
  object Conflict {
    final val status: Status = Status.Conflict
  }

  final case class InternalServerError(error: String, message: String)
      extends HttpError[InternalServerError](InternalServerError.status) derives Schema
  object InternalServerError {
    final val status: Status                        = Status.InternalServerError
    def apply(message: String): InternalServerError = InternalServerError("InternalServerError", message)
    val default: InternalServerError                = InternalServerError("Oops.... Something went wrong")
  }
}
