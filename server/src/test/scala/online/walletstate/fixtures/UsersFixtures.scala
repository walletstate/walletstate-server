package online.walletstate.fixtures

import online.walletstate.common.models.User
import java.util.UUID

trait UsersFixtures {

  import WalletsFixtures.ExistingWalletId

  // data that already exists in DB
  val ExistingUserId     = User.Id("existing-user-id")
  val ExistingUser: User = User(ExistingUserId, "existing-username", Some(ExistingWalletId))

  val ExistingUserWithoutWalletId1 = User.Id("existing-user-id-1")
  val ExistingUserWithoutWalletId2 = User.Id("existing-user-id-2")
  val ExistingUserWithoutWalletId3 = User.Id("existing-user-id-3")

  // new data for inserting
  val NewUserWithWallet    = User(User.Id("new-user-2"), "new-user-2-username", Some(ExistingWalletId))
  val NewUserWithoutWallet = User(User.Id("new-user-1"), "new-user-1-username", None)
}

object UsersFixtures extends UsersFixtures
