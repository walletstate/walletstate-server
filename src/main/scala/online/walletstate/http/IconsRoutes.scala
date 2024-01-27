package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Icon
import online.walletstate.services.IconsService
import zio.ZLayer
import zio.http.*
import zio.json.*

case class IconsRoutes(auth: AuthMiddleware, iconsService: IconsService) {

  private val createIconHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      iconContent <- req.body.asString
      icon        <- iconsService.create(ctx.wallet, iconContent)
    } yield Response.text(icon.id.id)
  }

  private val getIconHandler = Handler.fromFunctionZIO[(Icon.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      iconContent <- iconsService.get(ctx.wallet, id)
    } yield Response.text(iconContent)
  }

  private val getIconsIdsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      iconsIds <- iconsService.listIds(ctx.wallet)
    } yield Response.json(iconsIds.toJson)
  }

  val routes = Routes(
    Method.POST / "api" / "icons"               -> auth.walletCtx -> createIconHandler,
    Method.GET / "api" / "icons"                -> auth.walletCtx -> getIconsIdsHandler,
    Method.GET / "api" / "icons" / Icon.Id.path -> auth.walletCtx -> getIconHandler
  )

}

object IconsRoutes {
  val layer = ZLayer.fromFunction(IconsRoutes.apply _)
}
