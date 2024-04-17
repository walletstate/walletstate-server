package online.walletstate.models

import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

final case class Wallet(id: Wallet.Id, name: String, createdBy: User.Id)

object Wallet {
  final case class Id(id: UUID) extends AnyVal
  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("wallet-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
    given schema: Schema[Id]   = Schema[UUID].transform(Id(_), _.id)
  }

  def make(name: String, createdBy: User.Id): UIO[Wallet] =
    Id.random.map(Wallet(_, name, createdBy))

  given codec: JsonCodec[Wallet] = DeriveJsonCodec.gen[Wallet]
  given schema: Schema[Wallet] = DeriveSchema.gen[Wallet]
}
