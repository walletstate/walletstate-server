package online.walletstate.services

import online.walletstate.config.{AuthConfig, IdPConfig}
import online.walletstate.models.AppError.InvalidCredentials
import online.walletstate.models.api.LoginInfo
import online.walletstate.models.{AppError, User}
import zio.*
import zio.http.*

import java.security.MessageDigest
import java.util.HexFormat

trait IdentityProviderService {

  def loginUrl: UIO[URL]

  def authenticate(loginInfo: LoginInfo): IO[InvalidCredentials, User.Id]

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

  override val loginUrl: UIO[URL] = ZIO.succeed(URL(Path("/login"))) // move to config

  def authenticate(c: LoginInfo): IO[InvalidCredentials, User.Id] = for {
    user      <- ZIO.fromOption(config.users.find(_.username == c.username)).mapError(_ => InvalidCredentials())
    pwdDigest <- ZIO.succeed(MessageDigest.getInstance("SHA-256").digest(c.password.getBytes("UTF-8")))
    pwdHash   <- ZIO.succeed(HexFormat.of().formatHex(pwdDigest))
    _         <- if (user.passwordHash == pwdHash) ZIO.unit else ZIO.fail(InvalidCredentials())
  } yield User.Id(user.id)

}

final case class SSOIdentityProviderService(ssoConfig: IdPConfig.SSO) extends IdentityProviderService {

  override def loginUrl: UIO[URL] = ZIO.succeed(URL(Path("/url/to/idp/login/page"))) // from config

  override def authenticate(loginInfo: LoginInfo): IO[InvalidCredentials, User.Id] =
    ZIO.die(new NotImplementedError("Not applicable for SSO login"))
}
