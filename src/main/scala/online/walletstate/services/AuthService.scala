package online.walletstate.services

import online.walletstate.models.api.LoginInfo
import online.walletstate.models.{AppError, User, Wallet}
import zio.{Task, ZIO, ZLayer}

trait AuthService {

  def getOrCreateUser(loginInfo: LoginInfo): Task[User]

  def updateCurrentUserWallet(user: User.Id, wallet: Wallet.Id): Task[Wallet]

}

final case class AuthServiceLive(
    identityProviderService: IdentityProviderService,
    usersService: UsersService,
    walletsService: WalletsService
) extends AuthService {

  override def getOrCreateUser(loginInfo: LoginInfo): Task[User] = for {
    userId <- identityProviderService.authenticate(loginInfo)
    user <- usersService.get(userId).catchSome { case AppError.UserNotExist =>
      usersService.create(User(userId, loginInfo.username))
    }
  } yield user

  override def updateCurrentUserWallet(userId: User.Id, walletId: Wallet.Id): Task[Wallet] = for {
    isUserInWallet <- walletsService.isUserInWallet(userId, walletId)
    _              <- ZIO.cond(isUserInWallet, (), AppError.UserIsNotInWallet(userId, walletId))
    wallet         <- walletsService.get(walletId)
    _              <- usersService.setWallet(userId, walletId)
  } yield wallet
  
}

object AuthServiceLive {
  val layer = ZLayer.fromFunction(AuthServiceLive.apply _)
}
