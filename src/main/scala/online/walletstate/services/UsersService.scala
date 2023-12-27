package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.errors.UserNotExist
import online.walletstate.models.{Wallet, User}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.*

trait UsersService {
  def create(user: User): Task[User]
  def get(id: User.Id): Task[User]
  def setWallet(user: User.Id, wallet: Wallet.Id): Task[Unit]
}

final case class UsersServiceLive(quill: WalletStateQuillContext) extends UsersService {
  import io.getquill.*
  import quill.*

  override def create(user: User): Task[User] =
    run(insertUser(user)).map(_ => user)

  override def get(id: User.Id): Task[User] =
    run(userById(id)).map(_.headOption).getOrError(UserNotExist)

  override def setWallet(user: User.Id, wallet: Wallet.Id): Task[Unit] =
    run(updateWallet(user, wallet)).map(_ => ())

  // queries
  private inline def userById(id: User.Id)  = quote(query[User].filter(_.id == lift(id)))
  private inline def insertUser(user: User) = quote(query[User].insertValue(lift(user)))
  private inline def updateWallet(user: User.Id, wallet: Wallet.Id) =
    quote(userById(user).update(_.wallet -> Some(lift(wallet))))
}

object UsersServiceLive {
  val layer = ZLayer.fromFunction(UsersServiceLive.apply _)
}
