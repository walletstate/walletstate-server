package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, AuthRoutesHandler, UserContext, WalletContext}
import online.walletstate.models.Wallet
import online.walletstate.services.{TokenService, UsersService, WalletsService}
import online.walletstate.http.auth.AuthCookiesOps.withAuthCookies
import zio.*
import zio.http.*
import zio.json.*

final case class AuthRoutes(
    authRoutesHandler: AuthRoutesHandler,
    walletsService: WalletsService,
    tokenService: TokenService,
    usersService: UsersService,
    auth: AuthMiddleware
) {

  val changeCurrenWalletHandler = Handler.fromFunctionZIO[(Wallet.Id, UserContext, Request)] { (walletId, ctx, req) =>
    usersService.get(ctx.user).flatMap {
      case user if user.wallet.contains(walletId) =>
        for {
          wallet   <- walletsService.get(walletId)
          newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id))
        } yield Response.json(wallet.toJson).withAuthCookies(newToken)

      case _ =>
        ZIO.succeed(Response.forbidden("User doesn't have access to requested wallet"))
    }
  }

  def routes = Routes(
    Method.POST / "auth" / "login"                   -> handler { (req: Request) => authRoutesHandler.login(req) },
    Method.POST / "auth" / "logout"                  -> handler { (req: Request) => authRoutesHandler.logout(req) },
    Method.GET / "auth" / "callback"                 -> handler { (req: Request) => Response.notImplemented("boom") },
    Method.POST / "auth" / "wallet" / Wallet.Id.path -> auth.userCtx -> changeCurrenWalletHandler
  )

}

object AuthRoutes {
  val layer = ZLayer.fromFunction(AuthRoutes.apply _)
}
