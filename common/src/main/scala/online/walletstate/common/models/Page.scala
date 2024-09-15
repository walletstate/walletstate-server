package online.walletstate.common.models

import zio.Chunk
import zio.http.codec.QueryCodec
import online.walletstate.annotations.genericField
import zio.json.DeriveJsonCodec
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime
import java.util.Base64
import scala.util.Try

/** for now is used only for different representations of transaction/record
  */

final case class Page[T](@genericField() items: List[T], nextPage: Option[Page.Token])

object Page {

  case class Token(id: Record.Id, dt: ZonedDateTime)
  object Token {

    private val plainCodec = DeriveJsonCodec.gen[Token]

    private def base64Encode(stringJson: String): String =
      Base64.getEncoder.withoutPadding().encodeToString(stringJson.getBytes)

    private def base64Decode(base64String: String): Either[String, String] =
      Try(Base64.getDecoder.decode(base64String)).toEither
        .map(byteArray => new String(byteArray))
        .left
        .map(_.getMessage)

    // TODO make some more compact token
    given schema: Schema[Token] = Schema[String].transformOrFail[Token](
      string => base64Decode(string).flatMap(plainCodec.decoder.decodeJson),
      token => Right(base64Encode(plainCodec.encoder.encodeJson(token).toString))
    )

    val queryCodec: QueryCodec[Token] =
      QueryCodec
        .query[String]("page")
        .transformOrFail { string => base64Decode(string).flatMap(plainCodec.decoder.decodeJson) } { token =>
          Right(base64Encode(plainCodec.encoder.encodeJson(token).toString))
        }
  }

  given schema[T: Schema]: Schema[Page[T]] = DeriveSchema.gen[Page[T]]
}
