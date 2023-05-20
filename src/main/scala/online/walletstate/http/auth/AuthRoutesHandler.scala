package online.walletstate.http.auth

import online.walletstate.domain.auth.LoginInfo
import online.walletstate.domain.User
import online.walletstate.domain.errors.{AppHttpError, UserNotFound}
import online.walletstate.config.{AuthConfig, IdPConfig}
import online.walletstate.services.auth.TokenService
import online.walletstate.http.RequestOps.as
import online.walletstate.http.auth.AuthCookiesOps.{clearAuthCookies, withAuthCookies}
import online.walletstate.services.UsersService
import zio.*
import zio.http.*

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

  private def validateCreds(creds: LoginInfo) = ZIO
    .fromOption {
      config.users.find(u => u.username == creds.username && u.password == creds.password).map(_.id)
    }
    .mapError(e => AppHttpError(Status.Unauthorized, "Invalid credentials"))

  override def login(req: Request): Task[Response] = for {
    creds  <- req.as[LoginInfo]
    _      <- ZIO.logInfo(s"Creds $creds")
    userId <- validateCreds(creds)
    user  <- usersService.get(userId).catchSome { case UserNotFound => usersService.save(User(userId, creds.username)) }
    _     <- ZIO.logInfo(s"User login $user")
    token <- tokenService.encode(AuthContext.of(userId, user.namespace))
  } yield Response.text("logged in").withAuthCookies(token)

  override def logout(req: Request): Task[Response] =
    ZIO.succeed(Response.text("logged out").clearAuthCookies)
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
