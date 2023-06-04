package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.NamespaceInvite
import online.walletstate.models.errors.NamespaceInviteNotExist
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.*

trait NamespaceInvitesService {
  def save(invite: NamespaceInvite): Task[NamespaceInvite]
  def get(code: String): Task[NamespaceInvite]
  def delete(id: NamespaceInvite.Id): Task[Unit]
}

case class NamespaceInvitesServiceLive(quill: QuillCtx) extends NamespaceInvitesService {
  import io.getquill.*
  import quill.*

  override def save(invite: NamespaceInvite): Task[NamespaceInvite] =
    run(insert(invite)).map(_ => invite)

  override def get(code: String): Task[NamespaceInvite] =
    run(inviteByCode(code)).map(_.headOption).getOrError(NamespaceInviteNotExist)

  override def delete(id: NamespaceInvite.Id): Task[Unit] =
    run(inviteById(id).delete).map(_ => ())

  // queries
  private inline def inviteById(id: NamespaceInvite.Id) = quote(query[NamespaceInvite].filter(_.id == lift(id)))
  private inline def inviteByCode(code: String)      = quote(query[NamespaceInvite].filter(_.inviteCode == lift(code)))
  private inline def insert(invite: NamespaceInvite) = quote(query[NamespaceInvite].insertValue(lift(invite)))
}

object NamespaceInvitesServiceLive {
  val layer: ZLayer[QuillCtx, Nothing, NamespaceInvitesService] =
    ZLayer.fromFunction(NamespaceInvitesServiceLive.apply _)
}
