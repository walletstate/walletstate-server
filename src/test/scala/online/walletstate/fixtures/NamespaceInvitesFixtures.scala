package online.walletstate.fixtures

import online.walletstate.models.namespaces.NamespaceInvite

import java.time.Instant
import java.util.UUID

trait NamespaceInvitesFixtures {
  import NamespacesFixtures.ExistingNamespaceId
  import UsersFixtures.ExistingUserId

  val ExistingInvite = NamespaceInvite(
    UUID.fromString("2b3ce216-a0d2-4bf2-9a59-fbaeb03635e5"),
    ExistingNamespaceId,
    "TESTCODE",
    ExistingUserId,
    Instant.now().plusSeconds(3600)
  )

  val NewInvite = NamespaceInvite(
    UUID.randomUUID(),
    ExistingNamespaceId,
    "TESTCODE2",
    ExistingUserId,
    Instant.now().plusSeconds(600)
  )

  val AnotherNewInvite = NamespaceInvite(
    UUID.randomUUID(),
    ExistingNamespaceId,
    "TESTCODE3",
    ExistingUserId,
    Instant.now().plusSeconds(600)
  )
}

object NamespaceInvitesFixtures extends NamespaceInvitesFixtures
