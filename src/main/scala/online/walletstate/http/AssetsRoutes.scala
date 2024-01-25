package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Asset
import online.walletstate.models.api.CreateAsset
import online.walletstate.services.AssetsService
import online.walletstate.utils.RequestOps.as
import zio.ZLayer
import zio.http.*
import zio.json.*

final case class AssetsRoutes(auth: AuthMiddleware, assetsService: AssetsService) {

  private val createAssetHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      info  <- req.as[CreateAsset]
      asset <- assetsService.create(ctx.wallet, ctx.user, info)
    } yield Response.json(asset.toJson)
  }

  private val getAssetsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      assets <- assetsService.list(ctx.wallet)
    } yield Response.json(assets.toJson)
  }

  private val getAssetHandler = Handler.fromFunctionZIO[(Asset.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      asset <- assetsService.get(ctx.wallet, id)
    } yield Response.json(asset.toJson)
  }

  val routes = Routes(
    Method.POST / "api" / "assets"                -> auth.walletCtx -> createAssetHandler,
    Method.GET / "api" / "assets"                 -> auth.walletCtx -> getAssetsHandler,
    Method.GET / "api" / "assets" / Asset.Id.path -> auth.walletCtx -> getAssetHandler
  )

}

object AssetsRoutes {
  val layer = ZLayer.fromFunction(AssetsRoutes.apply _)
}
