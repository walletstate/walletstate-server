package online.walletstate.http.auth

import online.walletstate.config.{AuthConfig, IdPConfig}
import online.walletstate.http.auth.AuthCookiesOps.{clearAuthCookies, withAuthCookies}
import online.walletstate.models.User
import online.walletstate.models.api.LoginInfo
import online.walletstate.models.errors.{InvalidCredentials, UserNotExist}
import online.walletstate.services.{TokenService, UsersService}
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

import java.security.MessageDigest
import java.util.HexFormat

trait AuthRoutesHandler {

  def login(req: Request): Task[Response]

  def logout(req: Request): Task[Response]

}

object AuthRoutesHandler {

  val layer: ZLayer[TokenService with UsersService, Config.Error, AuthRoutesHandler] =
    ZLayer.fromZIO {
      ZIO
        .config(AuthConfig.config)
        .map(_.identityProvider)
        .flatMap {
          case cnf: IdPConfig.ConfiguredUsers =>
            for {
              ts <- ZIO.service[TokenService]
              us <- ZIO.service[UsersService]
            } yield ConfiguredUsersAuthRoutesHandler(cnf)(ts, us)

          case cnf: IdPConfig.SSO =>
            ZIO.service[TokenService].map(ts => SSOAuthRoutesHandler(cnf)())
        }
    }
}

class ConfiguredUsersAuthRoutesHandler(
    tokenService: TokenService,
    usersService: UsersService,
    config: IdPConfig.ConfiguredUsers
) extends AuthRoutesHandler {

  override def login(req: Request): Task[Response] = for {
    creds  <- req.as[LoginInfo]
    userId <- validateUserCredentials(creds)
    user   <- getOrCreateUser(userId, creds.username)
    token  <- tokenService.encode(AuthContext.of(userId, user.wallet))
  } yield Response.json(user.toJson).withAuthCookies(token)

  override def logout(req: Request): Task[Response] =
    ZIO.succeed(Response.text("logged out").clearAuthCookies)

  private def validateUserCredentials(c: LoginInfo): Task[User.Id] = for {
    user      <- ZIO.fromOption(config.users.find(_.username == c.username)).mapError(_ => InvalidCredentials)
    pwdDigest <- ZIO.attempt(MessageDigest.getInstance("SHA-256").digest(c.password.getBytes("UTF-8")))
    pwdHash   <- ZIO.attempt(HexFormat.of().formatHex(pwdDigest))
    _         <- if (user.passwordHash == pwdHash) ZIO.unit else ZIO.fail(InvalidCredentials)
  } yield User.Id(user.id)

  private def getOrCreateUser(userId: User.Id, username: String): Task[User] =
    usersService
      .get(userId)
      .catchSome { case UserNotExist =>
        usersService.create(User(userId, username))
      }
}

object ConfiguredUsersAuthRoutesHandler {
  def apply(
      config: IdPConfig.ConfiguredUsers
  )(tokenService: TokenService, usersService: UsersService): ConfiguredUsersAuthRoutesHandler =
    new ConfiguredUsersAuthRoutesHandler(tokenService, usersService, config)

//  def layer(config: IdPConfig.ConfiguredUsers): ZLayer[TokenService, Nothing, AuthRoutesHandler] =
//    ZLayer.fromFunction(apply(config) _)
}

class SSOAuthRoutesHandler() extends AuthRoutesHandler {
  override def login(req: Request): Task[Response] =
    ZIO.succeed(Response.text("todo login"))

  override def logout(req: Request): Task[Response] =
    ZIO.succeed(Response.text("todo logout"))
}

object SSOAuthRoutesHandler {
  def apply(config: IdPConfig.SSO)(): SSOAuthRoutesHandler = new SSOAuthRoutesHandler

//  def layer(config: IdPConfig.SSO) = ZLayer.fromFunction(apply(config) _)
}
