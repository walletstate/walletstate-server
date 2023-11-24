package online.walletstate.models

import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.time.Instant
import java.util.UUID

final case class WalletInvite(
    id: WalletInvite.Id,
    wallet: Wallet.Id,
    inviteCode: String,
    createdBy: User.Id,
    validTo: Instant
)

object WalletInvite {
  final case class Id(id: UUID) extends AnyVal
  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("wallet-invite-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(wallet: Wallet.Id, inviteCode: String, createdBy: User.Id, validTo: Instant): UIO[WalletInvite] =
    Id.random.map(WalletInvite(_, wallet, inviteCode, createdBy, validTo))

  given codec: JsonCodec[WalletInvite] = DeriveJsonCodec.gen[WalletInvite]
}
