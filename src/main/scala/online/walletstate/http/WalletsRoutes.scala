package online.walletstate.http

import online.walletstate.http.api.WalletsEndpoints
import online.walletstate.http.auth.*
import online.walletstate.models.{AppError, Wallet}
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import online.walletstate.services.*
import zio.*
import zio.http.*

final case class WalletsRoutes(auth: AuthMiddleware, walletsService: WalletsService) extends WalletsEndpoints {
  import auth.{implementWithUserCtx, implementWithWalletCtx}

  private val createRoute = create.implementWithUserCtx[(CreateWallet, UserContext)] {
    Handler.fromFunctionZIO((info, ctx) => walletsService.create(ctx.user, info.name))
  }()

  private val getCurrentRoute = getCurrent.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => walletsService.get(ctx.wallet))
  }()

  private val createInviteRoute = createInvite.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => walletsService.createInvite(ctx.user, ctx.wallet))
  }()

  private val joinRoute = join.implementWithUserCtx[(JoinWallet, UserContext)] {
    Handler.fromFunctionZIO((joinInfo, ctx) => walletsService.joinWallet(ctx.user, joinInfo.inviteCode))
  } {
    case e: AppError.WalletInviteNotExist.type => Left(Right(e))
    case e: AppError.WalletInviteExpired.type  => Right(e)
  }

  val routes = Routes(createRoute, getCurrentRoute, createInviteRoute, joinRoute)
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
