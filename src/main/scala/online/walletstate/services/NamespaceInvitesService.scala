package online.walletstate.services

import online.walletstate.models.namespaces.NamespaceInvite
import online.walletstate.models.namespaces.errors.NamespaceInviteNotExist
import online.walletstate.models.db.QuillCtx
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.*

import java.util.UUID

trait NamespaceInvitesService {
  def save(invite: NamespaceInvite): Task[NamespaceInvite]
  def get(code: String): Task[NamespaceInvite]
  def delete(id: UUID): Task[Unit]
}

case class NamespaceInvitesServiceLive(quill: QuillCtx) extends NamespaceInvitesService {
  import io.getquill.*
  import quill.*

  override def save(invite: NamespaceInvite): Task[NamespaceInvite] =
    run(insert(invite)).map(_ => invite)

  override def get(code: String): Task[NamespaceInvite] =
    run(inviteByCode(code)).map(_.headOption).getOrError(NamespaceInviteNotExist)

  override def delete(id: UUID): Task[Unit] =
    run(inviteById(id).delete).map(_ => ())

  // queries
  private inline def inviteById(id: UUID)            = quote(query[NamespaceInvite].filter(_.id == lift(id)))
  private inline def inviteByCode(code: String)      = quote(query[NamespaceInvite].filter(_.inviteCode == lift(code)))
  private inline def insert(invite: NamespaceInvite) = quote(query[NamespaceInvite].insertValue(lift(invite)))
}

object NamespaceInvitesServiceLive {
  val layer: ZLayer[QuillCtx, Nothing, NamespaceInvitesService] =
    ZLayer.fromFunction(NamespaceInvitesServiceLive.apply _)
}
