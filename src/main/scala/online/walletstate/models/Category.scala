package online.walletstate.models

import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

final case class Category(
    id: Category.Id,
    wallet: Wallet.Id,
    group: Group.Id,
    name: String,
    orderingIndex: Int,
    createdBy: User.Id
) extends Groupable

object Category {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("category-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(wallet: Wallet.Id, group: Group.Id, name: String, orderingIndex: Int, createdBy: User.Id): UIO[Category] =
    Id.random.map(Category(_, wallet, group, name, orderingIndex, createdBy))

  given codec: JsonCodec[Category] = DeriveJsonCodec.gen[Category]
}
