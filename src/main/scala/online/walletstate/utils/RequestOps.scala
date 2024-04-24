package online.walletstate.utils

import online.walletstate.models.AppError.ParseRequestError
import zio.*
import zio.http.*
import zio.http.Header.Accept.MediaTypeWithQFactor
import zio.json.*

object RequestOps {

  val defaultMediaTypes: NonEmptyChunk[MediaTypeWithQFactor] =
    NonEmptyChunk(MediaTypeWithQFactor(MediaType.application.`json`, Some(1)))

  extension (request: Request)
    def as[A: JsonDecoder]: Task[A] = for {
      json <- request.body.asString.mapError(e => ParseRequestError(s"Cannot read request body: ${e.getMessage}"))
      rs <- json.fromJson[A] match {
        case Left(error)  => ZIO.fail(ParseRequestError(error))
        case Right(value) => ZIO.succeed(value)
      }
    } yield rs

    def outputMediaType: NonEmptyChunk[MediaTypeWithQFactor] =
      NonEmptyChunk.fromChunk(request.headers.getAll(Header.Accept).flatMap(_.mimeTypes)).getOrElse(defaultMediaTypes)

}
