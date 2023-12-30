package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.WalletInvite
import online.walletstate.models.errors.WalletInviteNotExist
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.*

trait WalletInvitesService {
  def save(invite: WalletInvite): Task[WalletInvite]
  def get(code: String): Task[WalletInvite]
  def delete(id: WalletInvite.Id): Task[Unit]
}

case class WalletInvitesServiceLive(quill: WalletStateQuillContext) extends WalletInvitesService {
  import io.getquill.*
  import quill.*

  override def save(invite: WalletInvite): Task[WalletInvite] =
    run(insert(invite)).map(_ => invite)

  override def get(code: String): Task[WalletInvite] =
    run(inviteByCode(code)).map(_.headOption).getOrError(WalletInviteNotExist)

  override def delete(id: WalletInvite.Id): Task[Unit] =
    run(inviteById(id).delete).map(_ => ())

  // queries
  private inline def inviteById(id: WalletInvite.Id) = quote(query[WalletInvite].filter(_.id == lift(id)))
  private inline def inviteByCode(code: String)      = quote(query[WalletInvite].filter(_.inviteCode == lift(code)))
  private inline def insert(invite: WalletInvite)    = quote(query[WalletInvite].insertValue(lift(invite)))
}

object WalletInvitesServiceLive {
  val layer: ZLayer[WalletStateQuillContext, Nothing, WalletInvitesService] =
    ZLayer.fromFunction(WalletInvitesServiceLive.apply _)
}
