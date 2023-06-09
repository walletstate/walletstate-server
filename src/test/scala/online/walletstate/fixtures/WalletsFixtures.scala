package online.walletstate.fixtures

import online.walletstate.models.api.CreateWallet
import online.walletstate.models.Wallet

import java.util.UUID

trait WalletsFixtures {

  import UsersFixtures.*

  // data that already exists in DB
  val ExistingWalletId = Wallet.Id(UUID.fromString("0f41829c-6010-4170-b8d3-49813fb50e30"))
  val ExistingWallet   = Wallet(ExistingWalletId, "test-wallet", ExistingUserId)

  // new data for inserting

}

object WalletsFixtures extends WalletsFixtures
