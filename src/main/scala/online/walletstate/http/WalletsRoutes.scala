package online.walletstate.http

import online.walletstate.http.auth.*
import online.walletstate.http.auth.AuthCookiesOps.withAuthCookies
import online.walletstate.models.Wallet
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import online.walletstate.services.*
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.http.endpoint.*
import zio.json.*

final case class WalletsRoutes(
    auth: AuthMiddleware,
    walletsService: WalletsService,
    tokenService: TokenService
) {

  private val createWalletHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[UserContext]
      nsInfo   <- req.as[CreateWallet]
      wallet   <- walletsService.create(ctx.user, nsInfo.name)
      newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id))
    } yield Response.json(wallet.toJson).withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  private val getCurrentWalletHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx <- ZIO.service[WalletContext]
      ns  <- walletsService.get(ctx.wallet)
    } yield Response.json(ns.toJson)
  } @@ auth.ctx[WalletContext]

  private val inviteWalletHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx    <- ZIO.service[WalletContext]
      invite <- walletsService.createInvite(ctx.user, ctx.wallet)
    } yield Response.json(invite.toJson)
  } @@ auth.ctx[WalletContext]

  private val joinWalletHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[UserContext]
      joinInfo <- req.as[JoinWallet]
      wallet   <- walletsService.joinWallet(ctx.user, joinInfo.inviteCode)
      newToken <- tokenService.encode(WalletContext(ctx.user, wallet.id))
    } yield Response.json(wallet.toJson).withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  val routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "wallets"            => createWalletHandler
    case Method.GET -> !! / "api" / "wallets" / "current" => getCurrentWalletHandler
    case Method.POST -> !! / "api" / "wallets" / "invite" => inviteWalletHandler
    case Method.POST -> !! / "api" / "wallets" / "join"   => joinWalletHandler
  }
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
