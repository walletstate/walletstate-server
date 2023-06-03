package online.walletstate.fixtures

import online.walletstate.models.users.User

import java.util.UUID

trait UsersFixtures {

  import NamespacesFixtures.ExistingNamespaceId

  // data that already exists in DB
  val ExistingUserId     = "existing-user-id"
  val ExistingUser: User = User(ExistingUserId, "existing-username", Some(ExistingNamespaceId))

  val ExistingUserWithoutNamespaceId1 = "existing-user-id-1"
  val ExistingUserWithoutNamespaceId2 = "existing-user-id-2"
  val ExistingUserWithoutNamespaceId3 = "existing-user-id-3"

  // new data for inserting
  val NewUserWithNamespace    = User("new-user-2", "new-user-2-username", Some(ExistingNamespaceId))
  val NewUserWithoutNamespace = User("new-user-1", "new-user-1-username", None)
}

object UsersFixtures extends UsersFixtures
