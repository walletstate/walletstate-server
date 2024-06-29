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
}

object HttpError {

  final case class BadRequest(error: String, message: String) extends HttpError[BadRequest](Status.BadRequest)
      derives Schema

  final case class Unauthorized(error: String, message: String) extends HttpError[Unauthorized](Status.Unauthorized)
      derives Schema

  final case class Forbidden(error: String, message: String) extends HttpError[Forbidden](Status.Forbidden)
      derives Schema

  final case class NotFound(error: String, message: String) extends HttpError[NotFound](Status.NotFound) derives Schema

  final case class Conflict(error: String, message: String) extends HttpError[Conflict](Status.Conflict) derives Schema

  final case class InternalServerError(error: String, message: String)
      extends HttpError[InternalServerError](Status.InternalServerError) derives Schema
  object InternalServerError {
    def apply(message: String): InternalServerError = InternalServerError("InternalServerError", message)
    val default: InternalServerError                = InternalServerError("Oops.... Something went wrong")
  }
}
