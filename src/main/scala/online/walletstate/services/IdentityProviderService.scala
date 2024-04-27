package online.walletstate.services

import online.walletstate.config.{AuthConfig, IdPConfig}
import online.walletstate.utils.AuthCookiesOps.{clearAuthCookies, withAuthCookies}
import online.walletstate.http.auth.AuthContext
import online.walletstate.models.api.LoginInfo
import online.walletstate.models.{AppError, User}
import online.walletstate.services.{TokenService, UsersService}
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

import java.security.MessageDigest
import java.util.HexFormat

trait IdentityProviderService {

  def loginUrl: Task[URL]

  def authenticate(loginInfo: LoginInfo): Task[User.Id]

}

object IdentityProviderService {

  val layer =
    ZLayer.fromZIO {
      ZIO.config(AuthConfig.config).map(_.identityProvider).map {
        case cnf: IdPConfig.ConfiguredUsers => ConfiguredUsersIdentityProviderService(cnf)
        case cnf: IdPConfig.SSO             => SSOIdentityProviderService(cnf)
      }
    }
}

final case class ConfiguredUsersIdentityProviderService(config: IdPConfig.ConfiguredUsers)
    extends IdentityProviderService {
  
  override val loginUrl: Task[URL] = ZIO.succeed(URL(Path("/login"))) // move to config

  def authenticate(c: LoginInfo): Task[User.Id] = for {
    user      <- ZIO.fromOption(config.users.find(_.username == c.username)).mapError(_ => AppError.InvalidCredentials)
    pwdDigest <- ZIO.attempt(MessageDigest.getInstance("SHA-256").digest(c.password.getBytes("UTF-8")))
    pwdHash   <- ZIO.attempt(HexFormat.of().formatHex(pwdDigest))
    _         <- if (user.passwordHash == pwdHash) ZIO.unit else ZIO.fail(AppError.InvalidCredentials)
  } yield User.Id(user.id)

}

final case class SSOIdentityProviderService(ssoConfig: IdPConfig.SSO) extends IdentityProviderService {

  override def loginUrl: Task[URL] = ZIO.succeed(URL(Path("/url/to/idp/login/page"))) // from config

  override def authenticate(loginInfo: LoginInfo): Task[User.Id] =
    ZIO.fail(new NotImplementedError("Not applicable for SSO login"))
}
