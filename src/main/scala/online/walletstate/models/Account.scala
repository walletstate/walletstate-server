package online.walletstate.models

import online.walletstate.models
import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

final case class Account(
                          id: Account.Id,
                          group: Group.Id,
                          name: String,
                          orderingIndex: Int,
                          icon: String,
                          tags: Seq[String],
                          createdBy: User.Id
)

object Account {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("account-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(group: Group.Id, name: String, orderingIndex: Int, icon: String, tags: Seq[String], createdBy: User.Id): UIO[Account] =
    Id.random.map(Account(_, group, name, orderingIndex, icon, tags, createdBy))

  given codec: JsonCodec[Account] = DeriveJsonCodec.gen[Account]
}
