package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.{AppError, WalletInvite}
import online.walletstate.services.queries.WalletInvitesQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.*

trait WalletInvitesService {
  def save(invite: WalletInvite): Task[WalletInvite]
  def get(code: String): Task[WalletInvite]
  def delete(id: WalletInvite.Id): Task[Unit]
}

case class WalletInvitesServiceLive(quill: WalletStateQuillContext)
    extends WalletInvitesService
    with WalletInvitesQuillQueries {
  import io.getquill.*
  import quill.*

  override def save(invite: WalletInvite): Task[WalletInvite] =
    run(insert(invite)).map(_ => invite)

  override def get(code: String): Task[WalletInvite] =
    run(inviteByCode(code)).headOrError(AppError.WalletInviteNotExist)

  override def delete(id: WalletInvite.Id): Task[Unit] =
    run(inviteById(id).delete).map(_ => ())
}

object WalletInvitesServiceLive {
  val layer: ZLayer[WalletStateQuillContext, Nothing, WalletInvitesService] =
    ZLayer.fromFunction(WalletInvitesServiceLive.apply _)
}
