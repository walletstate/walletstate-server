package online.walletstate.common.models

import zio.http.codec.PathCodec
import zio.{UIO, ZIO}
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class User(id: User.Id, username: String, wallet: Option[Wallet.Id] = None)

object User {
  case class Id(id: String) extends AnyVal
  object Id {
    val path: PathCodec[Id] = zio.http.string("user-id").transform(Id(_))(_.id)

    given schema: Schema[Id]   = Schema[String].transform(Id(_), _.id)
    given codec: JsonCodec[Id] = zio.schema.codec.JsonCodec.jsonCodec(schema)
  }

  def make(id: User.Id, username: String, wallet: Option[Wallet.Id] = None): UIO[User] =
    ZIO.succeed(User(id, username, wallet))

  given schema: Schema[User]   = DeriveSchema.gen[User]
  given codec: JsonCodec[User] = zio.schema.codec.JsonCodec.jsonCodec(schema)

  final case class LoginInfo(username: String, password: String)

  object LoginInfo {
    given codec: JsonCodec[LoginInfo] = DeriveJsonCodec.gen[LoginInfo]
  }
}
