package online.walletstate.fixtures

import online.walletstate.models.User

import java.util.UUID

trait UsersFixtures {

  import NamespacesFixtures.ExistingNamespaceId

  // data that already exists in DB
  val ExistingUserId     = User.Id("existing-user-id")
  val ExistingUser: User = User(ExistingUserId, "existing-username", Some(ExistingNamespaceId))

  val ExistingUserWithoutNamespaceId1 = User.Id("existing-user-id-1")
  val ExistingUserWithoutNamespaceId2 = User.Id("existing-user-id-2")
  val ExistingUserWithoutNamespaceId3 = User.Id("existing-user-id-3")

  // new data for inserting
  val NewUserWithNamespace    = User(User.Id("new-user-2"), "new-user-2-username", Some(ExistingNamespaceId))
  val NewUserWithoutNamespace = User(User.Id("new-user-1"), "new-user-1-username", None)
}

object UsersFixtures extends UsersFixtures
