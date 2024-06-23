package online.walletstate.services

import online.walletstate.common.models.WalletInvite
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.WalletInviteNotExist
import online.walletstate.models.AppError
import online.walletstate.services.queries.WalletInvitesQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{UserIO, UserUIO, WalletUIO}
import zio.*

trait WalletInvitesService {
  def save(invite: WalletInvite): WalletUIO[WalletInvite]
  def get(code: String): UserIO[WalletInviteNotExist, WalletInvite]
  def delete(id: WalletInvite.Id): UserUIO[Unit]
}

case class WalletInvitesServiceLive(quill: WalletStateQuillContext)
    extends WalletInvitesService
    with WalletInvitesQuillQueries {
  import io.getquill.*
  import quill.*

  override def save(invite: WalletInvite): WalletUIO[WalletInvite] =
    run(insert(invite)).orDie.map(_ => invite)

  override def get(code: String): UserIO[WalletInviteNotExist, WalletInvite] =
    run(inviteByCode(code)).orDie.headOrError(WalletInviteNotExist())

  override def delete(id: WalletInvite.Id): UserUIO[Unit] =
    run(inviteById(id).delete).orDie.map(_ => ())
}

object WalletInvitesServiceLive {
  val layer: ZLayer[WalletStateQuillContext, Nothing, WalletInvitesService] =
    ZLayer.fromFunction(WalletInvitesServiceLive.apply _)
}
