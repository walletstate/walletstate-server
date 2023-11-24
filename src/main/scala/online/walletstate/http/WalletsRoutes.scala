package online.walletstate.http

import online.walletstate.http.auth.*
import online.walletstate.http.auth.AuthCookiesOps.withAuthCookies
import online.walletstate.models.Wallet
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import online.walletstate.services.*
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

final case class WalletsRoutes(
    auth: AuthMiddleware,
    walletsService: WalletsService,
    tokenService: TokenService
) {

  private val createWalletHandler = Handler.fromFunctionZIO[(UserContext, Request)] { (ctx, req) =>
    for {
      nsInfo   <- req.as[CreateWallet]
      wallet   <- walletsService.create(ctx.user, nsInfo.name)
      newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id))
    } yield Response.json(wallet.toJson).withAuthCookies(newToken)
  }

  private val getCurrentWalletHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, _) =>
    for {
      ns <- walletsService.get(ctx.wallet)
    } yield Response.json(ns.toJson)
  }

  private val inviteWalletHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, _) =>
    for {
      invite <- walletsService.createInvite(ctx.user, ctx.wallet)
    } yield Response.json(invite.toJson)
  }

  private val joinWalletHandler = Handler.fromFunctionZIO[(UserContext, Request)] { (ctx, req) =>
    for {
      joinInfo <- req.as[JoinWallet]
      wallet   <- walletsService.joinWallet(ctx.user, joinInfo.inviteCode)
      newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id))
    } yield Response.json(wallet.toJson).withAuthCookies(newToken)
  }

  val routes = Routes(
    Method.POST / "api" / "wallets"            -> auth.userCtx   -> createWalletHandler,
    Method.GET / "api" / "wallets" / "current" -> auth.walletCtx -> getCurrentWalletHandler,
    Method.POST / "api" / "wallets" / "invite" -> auth.walletCtx -> inviteWalletHandler,
    Method.POST / "api" / "wallets" / "join"   -> auth.userCtx   -> joinWalletHandler
  )
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
