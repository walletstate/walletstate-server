package online.walletstate.utils

import online.walletstate.models.errors.ParseError
import zio.*
import zio.http.*
import zio.json.*

object RequestOps {
  extension (request: Request)
    def as[A: JsonDecoder]: Task[A] = for {
      json <- request.body.asString.mapError(e => ParseError(s"Cannot read request body: ${e.getMessage}"))
      rs <- json.fromJson[A] match {
        case Left(error)  => ZIO.fail(ParseError(error))
        case Right(value) => ZIO.succeed(value)
      }
    } yield rs
}
