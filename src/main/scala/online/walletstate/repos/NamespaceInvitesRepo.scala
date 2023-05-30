package online.walletstate.repos

import online.walletstate.domain.namespaces.NamespaceInvite
import online.walletstate.domain.namespaces.errors.NamespaceInviteNotExist
import zio.*

import java.util.UUID

trait NamespaceInvitesRepo {
  def save(invite: NamespaceInvite): Task[NamespaceInvite]
  def get(code: String): Task[NamespaceInvite]
  def delete(id: UUID): Task[Unit]
}

case class NamespaceInvitesRepoLive(quill: QuillCtx) extends NamespaceInvitesRepo {
  import io.getquill.*
  import quill.*

  override def save(invite: NamespaceInvite): Task[NamespaceInvite] =
    run(quote(query[NamespaceInvite].insertValue(lift(invite)))).map(_ => invite)

  override def get(code: String): Task[NamespaceInvite] =
    run(quote(query[NamespaceInvite].filter(_.inviteCode == lift(code)))).map(_.headOption).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None        => ZIO.fail(NamespaceInviteNotExist)
    }

  override def delete(id: UUID): Task[Unit] =
    run(quote(query[NamespaceInvite]).filter(_.id == lift(id)).delete).map(_ => ())
}

object NamespaceInvitesRepoLive {
  val layer: ZLayer[QuillCtx, Nothing, NamespaceInvitesRepo] = ZLayer.fromFunction(NamespaceInvitesRepoLive.apply _)
}
