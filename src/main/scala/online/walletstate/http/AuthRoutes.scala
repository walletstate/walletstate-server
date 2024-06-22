package online.walletstate.http

import online.walletstate.http.endpoints.WalletStateEndpoints
import online.walletstate.models.AuthContext.{UserContext, WalletContext}
import online.walletstate.models.{AppError, AuthContext, HttpError, User, Wallet}
import online.walletstate.services.{AuthService, IdentityProviderService, TokenService, WalletsService}
import online.walletstate.utils.AuthCookiesOps.{clearAuthCookies, withAuthCookies}
import online.walletstate.utils.RequestOps.{as, outputMediaType}
import zio.*
import zio.http.*
import zio.json.*

final case class AuthRoutes(
    authService: AuthService,
    identityProviderService: IdentityProviderService,
    walletsService: WalletsService,
    tokenService: TokenService
) extends WalletStateRoutes
    with WalletStateEndpoints {

  val getLoginPageHandler = Handler.fromFunctionZIO { _ =>
    identityProviderService.loginUrl.map(url => Response.seeOther(url))
  }

  val loginHandler = Handler.fromFunctionZIO[Request] { req =>
    val res = for {
      creds <- req.as[User.LoginInfo]
      user  <- authService.getOrCreateUser(creds)
      token <- tokenService.encode(AuthContext.of(user.id, user.wallet, AuthContext.Type.Cookies))
    } yield Response.json(user.toJson).withAuthCookies(token)

    res.catchAll {
      case e: AppError.ParseRequestError  => HttpError.BadRequest(e).encodeZIO(req.outputMediaType)
      case e: AppError.InvalidCredentials => HttpError.Forbidden(e).encodeZIO(req.outputMediaType)
      case _                              => HttpError.InternalServerError.default.encodeZIO(req.outputMediaType)
    }
  }

  val logoutHandler = handler { (req: Request) => Response.ok.clearAuthCookies }

  val callbackHandler = handler { (req: Request) => Response.notImplemented("For future SSO") }

  val changeCurrenWalletHandler = Handler.fromFunctionZIO[(Wallet.Id, Request)] { (walletId, req) =>
    val rs = for {
      ctx      <- ZIO.service[UserContext]
      wallet   <- authService.updateCurrentUserWallet(walletId)
      newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id, AuthContext.Type.Cookies))
    } yield Response.json(wallet.toJson).withAuthCookies(newToken)

    rs.catchAll {
      case e: AppError.UserIsNotInWallet => HttpError.Forbidden(e).encodeZIO(req.outputMediaType)
      case _                             => HttpError.InternalServerError.default.encodeZIO(req.outputMediaType)
    }

  }

  override val noCtxRoutes = Routes(
    Method.GET / "auth" / "login"    -> getLoginPageHandler,
    Method.POST / "auth" / "login"   -> loginHandler,
    Method.POST / "auth" / "logout"  -> logoutHandler,
    Method.GET / "auth" / "callback" -> callbackHandler
  )

  override val userRoutes: Routes[UserContext, _] = Routes(
    Method.POST / "auth" / "wallet" / Wallet.Id.path -> changeCurrenWalletHandler
  )
}

object AuthRoutes {
  val layer = ZLayer.fromFunction(AuthRoutes.apply _)
}
