package online.walletstate.domain

import online.walletstate.domain.errors.{AppError, ToResponse}
import zio.http.Status
import zio.json.*

import java.time.Instant
import java.util.UUID

object namespaces {

  case class Namespace(id: UUID, name: String, createdBy: String)
  object Namespace {
    def apply(name: String, createdBy: String): Namespace = Namespace(UUID.randomUUID(), name, createdBy)
  }

  case class CreateNamespace(name: String)

  case class NamespaceInvite(id: UUID, namespaceId: UUID, inviteCode: String, createdBy: String, validTo: Instant)
  object NamespaceInvite {
    def apply(namespaceId: UUID, inviteCode: String, createdBy: String, validTo: Instant): NamespaceInvite =
      NamespaceInvite(UUID.randomUUID(), namespaceId, inviteCode, createdBy, validTo)
  }

  case class JoinNamespace(inviteCode: String)

  object codecs {
    given namespaceCodec: JsonCodec[Namespace]             = DeriveJsonCodec.gen[Namespace]
    given createNamespaceCodec: JsonCodec[CreateNamespace] = DeriveJsonCodec.gen[CreateNamespace]

    given namespaceInviteCodec: JsonCodec[NamespaceInvite] = DeriveJsonCodec.gen[NamespaceInvite]
    given joinNamespaceCodec: JsonCodec[JoinNamespace]     = DeriveJsonCodec.gen[JoinNamespace]
  }

  object errors {
    case object NamespaceNotExist extends AppError

    case object UserAlreadyHasNamespace // Allow only one namespace for user for now
        extends AppError
        with ToResponse(Status.Forbidden, "User already has the namespace")

    case object NamespaceInviteNotExist
        extends AppError
        with ToResponse(Status.Forbidden, "Invite code not found. Cannot join the namespace.")

    case object NamespaceInviteExpired
        extends AppError
        with ToResponse(Status.Forbidden, "Invite code expired. Please ask the namespace owner to generate a new one")

  }

}
