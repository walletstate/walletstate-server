package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.{AppError, User, Wallet, WalletInvite, WalletUser}
import online.walletstate.services.queries.WalletsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Clock, Task, ZIO, ZLayer}

import java.sql.SQLException
import scala.util.Random

trait WalletsService {
  def create(userId: User.Id, name: String): Task[Wallet]
  def get(id: Wallet.Id): Task[Wallet]
  def createInvite(userId: User.Id, wallet: Wallet.Id): Task[WalletInvite]
  def joinWallet(userId: User.Id, inviteCode: String): Task[Wallet]
  def isUserInWallet(user: User.Id, wallet: Wallet.Id): Task[Boolean]
}

case class WalletsServiceLive(
    quill: WalletStateQuillContext,
    usersService: UsersService,
    invitesService: WalletInvitesService
) extends WalletsService
    with WalletsQuillQueries {

  import io.getquill.*
  import quill.*

  override def create(userId: User.Id, name: String): Task[Wallet] = for {
    user   <- usersService.get(userId)
    wallet <- Wallet.make(name, user.id)
    _      <- transaction(run(insert(wallet)) *> run(insertWalletUser(WalletUser(wallet.id, userId))))
  } yield wallet

  override def get(id: Wallet.Id): Task[Wallet] = run(walletById(id)).headOrError(AppError.WalletNotExist)

  // TODO make expiration configurable and move to invites service
  override def createInvite(user: User.Id, wallet: Wallet.Id): Task[WalletInvite] = for {
    hasAccess <- isUserInWallet(user, wallet)
    _         <- ZIO.cond(hasAccess, (), AppError.WalletNotExist)
    now       <- Clock.instant
    code      <- ZIO.attempt(Random.alphanumeric.take(12).mkString.toUpperCase)
    invite    <- WalletInvite.make(wallet, code, user, now.plusSeconds(3600))
    _         <- invitesService.save(invite)
  } yield invite

  override def joinWallet(userId: User.Id, inviteCode: String): Task[Wallet] = for {
    user   <- usersService.get(userId)
    invite <- invitesService.get(inviteCode)
    now    <- Clock.instant
    _      <- if (invite.validTo.isBefore(now)) ZIO.fail(AppError.WalletInviteExpired) else ZIO.unit
    wallet <- get(invite.wallet)
    _      <- run(insertWalletUser(WalletUser(wallet.id, userId)))
    _      <- invitesService.delete(invite.id)
  } yield wallet

  override def isUserInWallet(user: User.Id, wallet: Wallet.Id): Task[Boolean] = run(userExists(wallet, user))
}

object WalletsServiceLive {
  val layer =
    ZLayer.fromFunction(WalletsServiceLive.apply _)

}
