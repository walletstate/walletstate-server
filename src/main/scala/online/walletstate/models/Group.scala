package online.walletstate.models

import online.walletstate.models
import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID
import scala.util.Try

final case class Group(
    id: Group.Id,
    wallet: Wallet.Id,
    `type`: Group.Type,
    name: String,
    orderingIndex: Int,
    createdBy: User.Id
)

object Group {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("group-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  enum Type {
    case Accounts, Categories
  }

  object Type {
    private def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr.capitalize)).toEither.left.map(_ => "Not a group type")

    def toString(`type`: Type): String = `type`.toString.toLowerCase

    val path: PathCodec[Type] = zio.http.string("group-type").transformOrFailLeft(fromString)(toString)

    given codec: JsonCodec[Type] = JsonCodec[String].transformOrFail(fromString, toString)
  }

  def make(wallet: Wallet.Id, `type`: Type, name: String, orderingIndex: Int, createdBy: User.Id): UIO[Group] =
    Id.random.map(Group(_, wallet, `type`, name, orderingIndex, createdBy))

  given codec: JsonCodec[Group] = DeriveJsonCodec.gen[Group]
}
