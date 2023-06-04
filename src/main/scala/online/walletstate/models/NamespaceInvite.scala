package online.walletstate.models

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.time.Instant
import java.util.UUID

final case class NamespaceInvite(
    id: NamespaceInvite.Id,
    namespaceId: Namespace.Id,
    inviteCode: String,
    createdBy: User.Id,
    validTo: Instant
)

object NamespaceInvite {
  final case class Id(id: UUID) extends AnyVal
  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(namespaceId: Namespace.Id, inviteCode: String, createdBy: User.Id, validTo: Instant): UIO[NamespaceInvite] =
    Id.random.map(NamespaceInvite(_, namespaceId, inviteCode, createdBy, validTo))

  given codec: JsonCodec[NamespaceInvite] = DeriveJsonCodec.gen[NamespaceInvite]
}
