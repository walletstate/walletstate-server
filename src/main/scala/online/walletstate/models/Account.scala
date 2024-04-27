package online.walletstate.models

import online.walletstate.models
import online.walletstate.models.api.CreateAccount
import zio.http.codec.{PathCodec, QueryCodec}
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, Random, Task, UIO, ZIO}

import java.util.UUID

final case class Account(
    id: Account.Id,
    group: Group.Id,
    name: String,
    idx: Int,
    icon: Option[Icon.Id],
    tags: List[String]
) extends Groupable

object Account {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id]   = zio.http.uuid("account-id").transform(Id(_))(_.id)
    val query: QueryCodec[Id] = QueryCodec.queryTo[UUID]("account").transform(Id(_))(_.id)
    
    given schema: Schema[Id]   = Schema[UUID].transform(Id(_), _.id)
  }

  def make(info: CreateAccount): UIO[Account] =
    Id.random.map(Account(_, info.group, info.name, info.idx, info.icon, info.tags))
  
  given schema: Schema[Account]   = DeriveSchema.gen[Account]
}
