package online.walletstate.services

import online.walletstate.common.models.{User, Wallet}
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.UserNotExist
import online.walletstate.models.AuthContext.UserContext
import online.walletstate.models.AppError
import online.walletstate.services.queries.UsersQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{UserIO, UserUIO}
import zio.*

trait UsersService {
  def create(user: User): UIO[User]
  def get(id: User.Id): IO[UserNotExist, User]
  def get: UserIO[UserNotExist, User]
  def setWallet(wallet: Wallet.Id): UserUIO[Unit]
}

final case class UsersServiceLive(quill: WalletStateQuillContext) extends UsersService with UsersQuillQueries {
  import io.getquill.*
  import quill.*

  override def create(user: User): UIO[User] =
    run(insertUser(user)).orDie.map(_ => user) // TODO handle conflicts

  override def get(id: User.Id): IO[UserNotExist, User] = run(userById(id)).orDie.headOrError(UserNotExist())

  override def get: UserIO[UserNotExist, User] = for {
    ctx  <- ZIO.service[UserContext]
    user <- run(userById(ctx.user)).orDie.headOrError(UserNotExist())
  } yield user

  override def setWallet(wallet: Wallet.Id): UserUIO[Unit] = for {
    ctx <- ZIO.service[UserContext]
    _   <- run(updateWallet(ctx.user, wallet)).orDie
  } yield ()

}

object UsersServiceLive {
  val layer = ZLayer.fromFunction(UsersServiceLive.apply _)
}
