package online.walletstate.models

import zio.{UIO, ZIO}
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class User(id: User.Id, username: String, namespace: Option[Namespace.Id] = None)

object User {
  case class Id(id: String) extends AnyVal
  object Id {
    given codec: JsonCodec[Id] = JsonCodec[String].transform(Id(_), _.id)
  }

  def make(id: User.Id, username: String, namespace: Option[Namespace.Id] = None): UIO[User] =
    ZIO.succeed(User(id, username, namespace))
  
  given codec: JsonCodec[User] = DeriveJsonCodec.gen[User]
}
