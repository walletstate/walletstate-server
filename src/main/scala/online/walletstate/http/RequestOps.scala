package online.walletstate.http

import zio.*
import zio.http.*
import zio.json.*

import scala.util.control.NoStackTrace

case class ParseError(msg: String) extends Exception with NoStackTrace
object ParseError {
  given encoder: JsonEncoder[ParseError] = DeriveJsonEncoder.gen[ParseError]
}

object RequestOps {
  extension (request: Request)
    def as[A: JsonDecoder]: Task[A] = for {
      json <- request.body.asString
      rs <- json.fromJson[A] match {
        case Left(error)  => ZIO.fail(ParseError(error))
        case Right(value) => ZIO.succeed(value)
      }
    } yield rs
}
