package online.walletstate.http

import online.walletstate.http.api.AssetsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Asset
import online.walletstate.models.api.{CreateAsset, UpdateAsset}
import online.walletstate.services.AssetsService
import zio.http.*
import zio.{Chunk, ZLayer}

final case class AssetsRoutes(auth: AuthMiddleware, assetsService: AssetsService) extends AssetsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(CreateAsset, WalletContext)] {
    Handler.fromFunctionZIO((info, ctx) => assetsService.create(ctx.wallet, ctx.user, info))
  }()

  private val listRoute = list.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => assetsService.list(ctx.wallet))
  }()

  private val listGroupedRoute = listGrouped.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => assetsService.grouped(ctx.wallet))
  }()

  private val getRoute = get.implementWithWalletCtx[(Asset.Id, WalletContext)] {
    Handler.fromFunctionZIO[(Asset.Id, WalletContext)]((id, ctx) => assetsService.get(ctx.wallet, id))
  }()

  private val updateRoute = update.implementWithWalletCtx[(Asset.Id, UpdateAsset, WalletContext)] {
    Handler.fromFunctionZIO((id, info, ctx) => assetsService.update(ctx.wallet, id, info))
  }()

  val routes = Routes(createRoute, listGroupedRoute, listRoute, getRoute, updateRoute)
}

object AssetsRoutes {
  val layer = ZLayer.fromFunction(AssetsRoutes.apply _)
}
