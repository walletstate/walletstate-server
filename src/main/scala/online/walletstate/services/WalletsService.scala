package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.*
import online.walletstate.models.{Wallet, WalletInvite, User}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Clock, Task, ZIO, ZLayer}

import scala.util.Random

trait WalletsService {
  def create(userId: User.Id, name: String): Task[Wallet]
  def get(id: Wallet.Id): Task[Wallet]
  def createInvite(userId: User.Id, wallet: Wallet.Id): Task[WalletInvite]
  def joinWallet(userId: User.Id, inviteCode: String): Task[Wallet]
}

case class WalletsServiceLive(
    quill: QuillCtx,
    usersService: UsersService,
    invitesService: WalletInvitesService
) extends WalletsService {

  import io.getquill.*
  import quill.*

  override def create(userId: User.Id, name: String): Task[Wallet] = for {
    user      <- usersService.get(userId)
    _         <- if (user.wallet.nonEmpty) ZIO.fail(UserAlreadyHasWallet) else ZIO.unit
    wallet <- Wallet.make(name, user.id)
    _         <- run(insert(wallet))
    _         <- usersService.setWallet(user.id, wallet.id)
  } yield wallet

  override def get(id: Wallet.Id): Task[Wallet] =
    run(walletById(id)).map(_.headOption).getOrError(WalletNotExist)

  // TODO make expiration configurable and move to invites service
  override def createInvite(userId: User.Id, wallet: Wallet.Id): Task[WalletInvite] = for {
    now    <- Clock.instant
    code   <- ZIO.attempt(Random.alphanumeric.take(12).mkString.toUpperCase)
    invite <- WalletInvite.make(wallet, code, userId, now.plusSeconds(3600))
    _      <- invitesService.save(invite)
  } yield invite

  override def joinWallet(userId: User.Id, inviteCode: String): Task[Wallet] = for {
    user      <- usersService.get(userId)
    _         <- if (user.wallet.nonEmpty) ZIO.fail(UserAlreadyHasWallet) else ZIO.unit
    invite    <- invitesService.get(inviteCode)
    now       <- Clock.instant
    _         <- if (invite.validTo.isBefore(now)) ZIO.fail(WalletInviteExpired) else ZIO.unit
    wallet <- get(invite.wallet)
    _         <- usersService.setWallet(userId, invite.wallet)
    _         <- invitesService.delete(invite.id)
  } yield wallet

  // queries
  private inline def insert(ns: Wallet)           = quote(query[Wallet].insertValue(lift(ns)))
  private inline def walletById(id: Wallet.Id) = quote(query[Wallet].filter(_.id == lift(id)))

}

object WalletsServiceLive {
  val layer =
    ZLayer.fromFunction(WalletsServiceLive.apply _)

}
