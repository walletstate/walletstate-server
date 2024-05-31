package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.{UserNotExist, WalletInviteExpired, WalletInviteNotExist, WalletNotExist}
import online.walletstate.models.AuthContext.{UserContext, WalletContext}
import online.walletstate.models.*
import online.walletstate.services.queries.WalletsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{UserIO, WalletIO}
import zio.{Clock, UIO, ZIO, ZLayer}

import java.sql.SQLException
import scala.util.Random

trait WalletsService {
  type JoinWalletError = UserNotExist | WalletInviteNotExist | WalletInviteExpired | WalletNotExist

  def create(name: String): UserIO[UserNotExist, Wallet]
  def get(wallet: Wallet.Id): UserIO[WalletNotExist, Wallet]
  def get: WalletIO[WalletNotExist, Wallet]
  def createInvite: WalletIO[WalletNotExist, WalletInvite]
  def joinWallet(inviteCode: String): UserIO[JoinWalletError, Wallet]
  def isUserInWallet(user: User.Id, wallet: Wallet.Id): UIO[Boolean]
}

case class WalletsServiceLive(
    quill: WalletStateQuillContext,
    usersService: UsersService,
    invitesService: WalletInvitesService
) extends WalletsService
    with WalletsQuillQueries {

  import io.getquill.*
  import quill.*

  override def create(name: String): UserIO[UserNotExist, Wallet] = for {
    ctx    <- ZIO.service[UserContext]
    user   <- usersService.get(ctx.user)
    wallet <- Wallet.make(name, user.id)
    _      <- transaction(run(insert(wallet)) *> run(insertWalletUser(WalletUser(wallet.id, ctx.user)))).orDie
  } yield wallet

  override def get(wallet: Wallet.Id): UserIO[WalletNotExist, Wallet] =
    run(walletById(wallet)).orDie.headOrError(WalletNotExist())

  override def get: WalletIO[WalletNotExist, Wallet] = for {
    ctx    <- ZIO.service[WalletContext]
    wallet <- run(walletById(ctx.wallet)).orDie.headOrError(WalletNotExist())
  } yield wallet

  // TODO make expiration configurable and move to invites service
  override def createInvite: WalletIO[WalletNotExist, WalletInvite] = for {
    ctx       <- ZIO.service[WalletContext]
    hasAccess <- isUserInWallet(ctx.user, ctx.wallet)
    _         <- ZIO.cond(hasAccess, (), WalletNotExist())
    now       <- Clock.instant
    code      <- ZIO.succeed(Random.alphanumeric.take(12).mkString.toUpperCase)
    invite    <- WalletInvite.make(ctx.wallet, code, ctx.user, now.plusSeconds(3600))
    _         <- invitesService.save(invite)
  } yield invite

  override def joinWallet(inviteCode: String): UserIO[JoinWalletError, Wallet] = for {
    ctx    <- ZIO.service[UserContext]
    user   <- usersService.get(ctx.user)
    invite <- invitesService.get(inviteCode)
    now    <- Clock.instant
    _      <- if (invite.validTo.isBefore(now)) ZIO.fail(WalletInviteExpired()) else ZIO.unit
    wallet <- get(invite.wallet)
    _      <- run(insertWalletUser(WalletUser(wallet.id, ctx.user))).orDie
    _      <- invitesService.delete(invite.id)
  } yield wallet

  override def isUserInWallet(user: User.Id, wallet: Wallet.Id): UIO[Boolean] =
    run(userExists(wallet, user)).orDie
}

object WalletsServiceLive {
  val layer = ZLayer.fromFunction(WalletsServiceLive.apply _)
}
