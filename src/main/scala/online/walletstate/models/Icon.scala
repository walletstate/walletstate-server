package online.walletstate.models

import zio.{Chunk, Task, ZIO}
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

  def make(wallet: Wallet.Id, contentType: String, content: String, tags: List[String] = List.empty): Task[Icon] =
    for {
      contentDigest <- ZIO.attempt(MessageDigest.getInstance("SHA-256").digest(content.getBytes("UTF-8")))
      contentHash   <- ZIO.attempt(HexFormat.of().formatHex(contentDigest))
      iconId        <- ZIO.fromEither(Id.make(contentHash)).mapError(e => AppError.InvalidIconId(e))
    } yield Icon(Some(wallet), iconId, contentType, content, tags)

  given schema: Schema[Icon] = DeriveSchema.gen[Icon]
}
