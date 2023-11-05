package online.walletstate.models

import online.walletstate.models
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

final case class AccountsGroup(
    id: AccountsGroup.Id,
    wallet: Wallet.Id,
    name: String,
    orderingIndex: Int,
    createdBy: User.Id
)

object AccountsGroup {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(wallet: Wallet.Id, name: String, orderingIndex: Int, createdBy: User.Id): UIO[AccountsGroup] =
    Id.random.map(AccountsGroup(_, wallet, name, orderingIndex, createdBy))

  given codec: JsonCodec[AccountsGroup] = DeriveJsonCodec.gen[AccountsGroup]
}
