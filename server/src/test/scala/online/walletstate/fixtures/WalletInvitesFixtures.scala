package online.walletstate.fixtures

import online.walletstate.common.models.WalletInvite
import java.time.Instant
import java.util.UUID

trait WalletInvitesFixtures {
  import WalletsFixtures.ExistingWalletId
  import UsersFixtures.ExistingUserId

  val ExistingInvite = WalletInvite(
    WalletInvite.Id(UUID.fromString("2b3ce216-a0d2-4bf2-9a59-fbaeb03635e5")),
    ExistingWalletId,
    "TESTCODE",
    ExistingUserId,
    Instant.now().plusSeconds(3600)
  )

  val NewInvite = WalletInvite(
    WalletInvite.Id(UUID.randomUUID()),
    ExistingWalletId,
    "TESTCODE2",
    ExistingUserId,
    Instant.now().plusSeconds(600)
  )

  val AnotherNewInvite = WalletInvite(
    WalletInvite.Id(UUID.randomUUID()),
    ExistingWalletId,
    "TESTCODE3",
    ExistingUserId,
    Instant.now().plusSeconds(600)
  )
}

object WalletInvitesFixtures extends WalletInvitesFixtures
