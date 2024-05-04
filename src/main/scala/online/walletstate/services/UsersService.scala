package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.UserNotExist
import online.walletstate.models.{User, Wallet}
import online.walletstate.services.queries.UsersQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.*

trait UsersService {
  def create(user: User): Task[User]
  def get(id: User.Id): Task[User]
  def setWallet(user: User.Id, wallet: Wallet.Id): Task[Unit]
}

final case class UsersServiceLive(quill: WalletStateQuillContext) extends UsersService with UsersQuillQueries {
  import io.getquill.*
  import quill.*

  override def create(user: User): Task[User] =
    run(insertUser(user)).map(_ => user)

  override def get(id: User.Id): Task[User] =
    run(userById(id)).headOrError(UserNotExist)

  override def setWallet(user: User.Id, wallet: Wallet.Id): Task[Unit] =
    run(updateWallet(user, wallet)).map(_ => ())
}

object UsersServiceLive {
  val layer = ZLayer.fromFunction(UsersServiceLive.apply _)
}
