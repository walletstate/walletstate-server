package online.walletstate.models

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

final case class Account(id: Account.Id, wallet: Wallet.Id, name: String, createdBy: User.Id)

object Account {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(wallet: Wallet.Id, name: String, createdBy: User.Id): UIO[Account] =
    Id.random.map(Account(_, wallet, name, createdBy))

  given codec: JsonCodec[Account] = DeriveJsonCodec.gen[Account]
}
