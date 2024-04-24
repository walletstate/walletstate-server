package online.walletstate.http

import online.walletstate.http.auth.{AuthContext, AuthMiddleware, UserContext, WalletContext}
import online.walletstate.models.{AppError, Wallet}
import online.walletstate.utils.RequestOps.{as, outputMediaType}
import online.walletstate.utils.AuthCookiesOps.clearAuthCookies
import online.walletstate.services.{AuthService, IdentityProviderService, TokenService, UsersService, WalletsService}
import online.walletstate.utils.AuthCookiesOps.withAuthCookies
import online.walletstate.models.api.LoginInfo
import zio.*
import zio.http.*
import zio.json.*

final case class AuthRoutes(
    authService: AuthService,
    identityProviderService: IdentityProviderService,
    walletsService: WalletsService,
    tokenService: TokenService,
    auth: AuthMiddleware
) {

  val getLoginPageHandler = Handler.fromFunctionZIO { _ =>
    identityProviderService.loginUrl.map(url => Response.seeOther(url))
  }

  val loginHandler = Handler.fromFunctionZIO[Request] { req =>
    val res = for {
      creds <- req.as[LoginInfo]
      user  <- authService.getOrCreateUser(creds)
      token <- tokenService.encode(AuthContext.of(user.id, user.wallet))
    } yield Response.json(user.toJson).withAuthCookies(token)

    res.catchAll {
      case e: AppError.ParseRequestError       => e.encode(Status.BadRequest, req.outputMediaType)
      case e: AppError.InvalidCredentials.type => e.encode(Status.Forbidden, req.outputMediaType)
      case _ => AppError.InternalServerError.encode(Status.InternalServerError, req.outputMediaType)
    }
  }

  val logoutHandler = handler { (req: Request) => Response.ok.clearAuthCookies }

  val callbackHandler = handler { (req: Request) => Response.notImplemented("For future SSO") }

  val changeCurrenWalletHandler = Handler.fromFunctionZIO[(Wallet.Id, UserContext, Request)] { (walletId, ctx, req) =>
    val rs = for {
      wallet   <- authService.updateCurrentUserWallet(ctx.user, walletId)
      newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id))
    } yield Response.json(wallet.toJson).withAuthCookies(newToken)

    rs.catchAll {
      case e: AppError.UserIsNotInWallet => e.encode(Status.Forbidden, req.outputMediaType)
      case _ => AppError.InternalServerError.encode(Status.InternalServerError, req.outputMediaType)
    }
  }

  def routes = Routes(
    Method.GET / "auth" / "login"                    -> getLoginPageHandler,
    Method.POST / "auth" / "login"                   -> loginHandler,
    Method.POST / "auth" / "logout"                  -> logoutHandler,
    Method.GET / "auth" / "callback"                 -> callbackHandler,
    Method.POST / "auth" / "wallet" / Wallet.Id.path -> auth.userCtx -> changeCurrenWalletHandler
  )

}

object AuthRoutes {
  val layer = ZLayer.fromFunction(AuthRoutes.apply _)
}
