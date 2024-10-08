package online.walletstate.http

import online.walletstate.http.endpoints.WalletsEndpoints
import online.walletstate.models.AppError.{UserNotExist, WalletInviteExpired, WalletInviteNotExist, WalletNotExist}
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.{AppError, AuthContext}
import online.walletstate.services.{TokenService, WalletsService}
import zio.*
import zio.http.*

final case class WalletsRoutes(walletsService: WalletsService, tokenService: TokenService)
    extends WalletStateRoutes
    with WalletsEndpoints {

  private val createRoute = createEndpoint.implement(info => walletsService.create(info.name).mapError(_.asNotFound))
  private val getCurrentRoute = getCurrentEndpoint.implement(_ => walletsService.get.mapError(_.asNotFound))
  private val createInviteRoute =
    createInviteEndpoint.implement(_ => walletsService.createInvite.mapError(_.asNotFound))

  private val joinRoute = joinEndpoint.implement { info =>
    walletsService.joinWallet(info.inviteCode).mapError {
      case e: UserNotExist         => e.asNotFound
      case e: WalletNotExist       => e.asNotFound
      case e: WalletInviteNotExist => e.asNotFound
      case e: WalletInviteExpired  => e.asForbidden
    }
  }

  // TODO Move to separate service
  private val createApiTokenRoute = createApiToken.implement { data =>
    for {
      ctx        <- ZIO.service[WalletContext]
      isInWallet <- walletsService.isUserInWallet(ctx.user, ctx.wallet)
      _          <- ZIO.cond(isInWallet, (), AppError.UserIsNotInWallet(ctx.user, ctx.wallet).asForbidden)
      _          <- ZIO.cond(ctx.isCookiesCtx, (), AppError.CreateAPITokenNotAllowed.asForbidden)
      token      <- tokenService.encode(ctx.copy(`type` = AuthContext.Type.Bearer), data.expireAt)
    } yield token
  }

  override val userRoutes   = Routes(createRoute, joinRoute)
  override val walletRoutes = Routes(getCurrentRoute, createInviteRoute, createApiTokenRoute)
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
