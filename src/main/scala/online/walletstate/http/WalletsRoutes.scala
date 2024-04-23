package online.walletstate.http

import online.walletstate.http.api.endpoints.WalletsEndpoints
import online.walletstate.http.auth.*
import online.walletstate.models.Wallet
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import online.walletstate.services.*
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

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
  }()

  val routes = Routes(createRoute, getCurrentRoute, createInviteRoute, joinRoute)
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
