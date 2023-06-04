package online.walletstate.fixtures

import online.walletstate.models.api.CreateNamespace
import online.walletstate.models.Namespace

import java.util.UUID

trait NamespacesFixtures {

  import UsersFixtures.*

  // data that already exists in DB
  val ExistingNamespaceId = Namespace.Id(UUID.fromString("0f41829c-6010-4170-b8d3-49813fb50e30"))
  val ExistingNamespace   = Namespace(ExistingNamespaceId, "test-namespace", ExistingUserId)

  // new data for inserting

}

object NamespacesFixtures extends NamespacesFixtures
