package online.walletstate.fixtures

import online.walletstate.models.namespaces.{CreateNamespace, Namespace}

import java.util.UUID

trait NamespacesFixtures {

  import UsersFixtures.*

  // data that already exists in DB
  val ExistingNamespaceId = UUID.fromString("0f41829c-6010-4170-b8d3-49813fb50e30")
  val ExistingNamespace   = Namespace(ExistingNamespaceId, "test-namespace", ExistingUserId)

  // new data for inserting
  val CreateNamespace1 = CreateNamespace("test-namespace-1")
  val CreateNamespace2 = CreateNamespace("test-namespace-2")
  val CreateNamespace3 = CreateNamespace("test-namespace-3")
}

object NamespacesFixtures extends NamespacesFixtures
