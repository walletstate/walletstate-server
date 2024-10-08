package online.walletstate.common.models

import zio.http.codec.{PathCodec, QueryCodec}
import zio.schema.{derived, Schema}
import zio.{Chunk, Random, Task, UIO, ZIO}

import java.util.UUID

final case class Account(
    id: Account.Id,
    wallet: Wallet.Id,
    group: Group.Id,
    name: String,
    defaultAsset: Option[Asset.Id],
    idx: Int,
    icon: Option[Icon.Id],
    tags: List[String],
    externalId: Option[String]
) extends Groupable
    derives Schema

object Account {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id]   = zio.http.uuid("account-id").transform(Id(_))(_.id)
    val query: QueryCodec[Id] = QueryCodec.query[UUID]("account").transform(Id(_))(_.id)

    given schema: Schema[Id] = Schema[UUID].transform(Id(_), _.id)
  }

  def make(wallet: Wallet.Id, data: Data): UIO[Account] =
    Id.random.map(
      Account(_, wallet, data.group, data.name, data.defaultAsset, data.idx, data.icon, data.tags, data.externalId)
    )

  final case class Data(
      group: Group.Id,
      name: String,
      defaultAsset: Option[Asset.Id],
      idx: Int,
      icon: Option[Icon.Id],
      tags: List[String],
      externalId: Option[String]
  ) derives Schema

}
