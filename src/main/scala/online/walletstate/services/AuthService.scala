package online.walletstate.services

import online.walletstate.UserIO
import online.walletstate.models.AppError.{InvalidCredentials, UserIsNotInWallet, WalletNotExist}
import online.walletstate.models.AuthContext.UserContext
import online.walletstate.models.{AppError, User, Wallet}
import zio.{IO, ZIO, ZLayer}

trait AuthService {

  def getOrCreateUser(loginInfo: User.LoginInfo): IO[InvalidCredentials, User]

  def updateCurrentUserWallet(wallet: Wallet.Id): UserIO[UserIsNotInWallet | WalletNotExist, Wallet]

}

final case class AuthServiceLive(
    identityProviderService: IdentityProviderService,
    usersService: UsersService,
    walletsService: WalletsService
) extends AuthService {

  override def getOrCreateUser(loginInfo: User.LoginInfo): IO[InvalidCredentials, User] = for {
    userId <- identityProviderService.authenticate(loginInfo)
    user   <- usersService.get(userId).catchAll(e => usersService.create(User(userId, loginInfo.username)))
  } yield user

  def updateCurrentUserWallet(walletId: Wallet.Id): UserIO[UserIsNotInWallet | WalletNotExist, Wallet] = for {
    ctx            <- ZIO.service[UserContext]
    isUserInWallet <- walletsService.isUserInWallet(ctx.user, walletId)
    _              <- ZIO.cond(isUserInWallet, (), UserIsNotInWallet(ctx.user, walletId))
    wallet         <- walletsService.get(walletId)
    _              <- usersService.setWallet(walletId)
  } yield wallet

}

object AuthServiceLive {
  val layer = ZLayer.fromFunction(AuthServiceLive.apply _)
}
