package online.walletstate.domain

import zio.json.*

import java.time.Instant
import java.util.UUID

object namespaces {
  
  case class Namespace(id: UUID, name: String, createdBy: String, createdAt: Instant)

  case class CreateNamespace(name: String)

  case class NamespaceInvite(id: UUID, namespaceId: UUID, inviteCode: String, validTo: Instant)
  case class JoinNamespace(namespace: UUID, inviteCode: String)

  object codecs {
    given namespaceCodec: JsonCodec[Namespace]             = DeriveJsonCodec.gen[Namespace]
    given createNamespaceCodec: JsonCodec[CreateNamespace] = DeriveJsonCodec.gen[CreateNamespace]

    given namespaceInviteCodec: JsonCodec[NamespaceInvite] = DeriveJsonCodec.gen[NamespaceInvite]
    given joinNamespaceCodec: JsonCodec[JoinNamespace]     = DeriveJsonCodec.gen[JoinNamespace]
  }
  
  object errors {
    
  }

}
