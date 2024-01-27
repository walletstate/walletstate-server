package online.walletstate.models

import online.walletstate.models.errors.InvalidIconId
import zio.{Task, ZIO}
import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.security.MessageDigest
import java.util.HexFormat

final case class Icon(wallet: Wallet.Id, id: Icon.Id, content: String)

object Icon {

  case class Id(id: String) extends AnyVal

  object Id {
    // TODO Make more strict validation
    def make(id: String): Either[String, Id] =
      if (id.length == 64) Right(Id(id)) else Left("Icon id must be a sh256 hash value")

    val path: PathCodec[Id] = zio.http.string("icon-id").transformOrFailLeft(make)(_.id)

    given codec: JsonCodec[Id] = JsonCodec[String].transformOrFail(make, _.id)
  }

  def make(wallet: Wallet.Id, content: String): Task[Icon] = for {
    contentDigest <- ZIO.attempt(MessageDigest.getInstance("SHA-256").digest(content.getBytes("UTF-8")))
    contentHash   <- ZIO.attempt(HexFormat.of().formatHex(contentDigest))
    iconId        <- ZIO.fromEither(Id.make(contentHash)).mapError(e => InvalidIconId(e))
  } yield Icon(wallet, iconId, content)

  given codec: JsonCodec[Icon] = DeriveJsonCodec.gen[Icon]
}
