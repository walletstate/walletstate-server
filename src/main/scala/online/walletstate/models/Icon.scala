package online.walletstate.models

import online.walletstate.models.AppError.InvalidIconId
import zio.{Chunk, IO, Task, ZIO}
import zio.http.codec.PathCodec
import zio.schema.{DeriveSchema, Schema}

import java.security.MessageDigest
import java.util.HexFormat

final case class Icon(wallet: Option[Wallet.Id], id: Icon.Id, contentType: String, content: String, tags: List[String])

object Icon {

  case class Id(id: String) extends AnyVal

  object Id {
    // TODO Make more strict validation
    def make(id: String): Either[String, Id] =
      if (id.length == 64) Right(Id(id)) else Left("Icon id must be a sh256 hash value")

    //TODO Investigate. Path validation cause 500 response but should 404 or 400
    val path: PathCodec[Id] = zio.http.string("icon-id").transformOrFailLeft(make)(_.id)

    given schema: Schema[Id] = Schema[String].transformOrFail(make, id => Right(id.id))
  }

  def make(wallet: Wallet.Id, contentType: String, content: String, tags: List[String] = List.empty): IO[InvalidIconId, Icon] =
    for {
      contentDigest <- ZIO.succeed(MessageDigest.getInstance("SHA-256").digest(content.getBytes("UTF-8")))
      contentHash   <- ZIO.succeed(HexFormat.of().formatHex(contentDigest))
      iconId        <- ZIO.fromEither(Id.make(contentHash)).mapError(e => InvalidIconId(e))
    } yield Icon(Some(wallet), iconId, contentType, content, tags)

  given schema: Schema[Icon] = DeriveSchema.gen[Icon]
}
