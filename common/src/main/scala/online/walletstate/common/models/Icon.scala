package online.walletstate.common.models

import zio.Chunk
import zio.http.codec.PathCodec
import zio.schema.{Schema, derived}

final case class Icon(wallet: Option[Wallet.Id], id: Icon.Id, contentType: String, content: String, tags: List[String])
    derives Schema

object Icon {

  case class Id(id: String) extends AnyVal

  object Id {
    // TODO Make more strict validation
    def make(id: String): Either[String, Id] =
      if (id.length == 64) Right(Id(id)) else Left("Icon id must be a sh256 hash value")

    // TODO Investigate. Path validation cause 500 response but should 404 or 400
    val path: PathCodec[Id] = zio.http.string("icon-id").transformOrFailLeft(make)(_.id)

    given schema: Schema[Id] = Schema[String].transformOrFail(make, id => Right(id.id))
  }

  final case class Data(contentType: String, content: String, tags: List[String] = List.empty) derives Schema
}
