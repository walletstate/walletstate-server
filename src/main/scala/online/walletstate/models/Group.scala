package online.walletstate.models

import online.walletstate.models
import zio.http.codec.PathCodec
import zio.schema.{DeriveSchema, Schema}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID
import scala.util.Try

final case class Group(
    id: Group.Id,
    wallet: Wallet.Id,
    `type`: Group.Type,
    name: String,
    idx: Int
)

object Group {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("group-id").transform(Id(_))(_.id)

    given schema: Schema[Id]   = Schema[UUID].transform(Id(_), _.id)
  }

  enum Type {
    case Accounts, Categories
  }

  object Type {
    def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr)).toEither.left.map(_ => s"$typeStr is not a group type")

    def asString(`type`: Type): String = `type`.toString
  }

  def make(wallet: Wallet.Id, `type`: Type, name: String, idx: Int): UIO[Group] =
    Id.random.map(Group(_, wallet, `type`, name, idx))

  given schema: Schema[Group]   = DeriveSchema.gen[Group]
}
