package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Icon
import online.walletstate.services.IconsService
import zio.{Chunk, ZLayer}
import zio.http.*
import zio.json.*

import java.util.Base64

case class IconsRoutes(auth: AuthMiddleware, iconsService: IconsService) {

  private val createIconHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      iconContent <- req.body.asString
      icon        <- iconsService.create(ctx.wallet, iconContent)
    } yield Response.json(icon.id.toJson)
  }

  private val getIconHandler = Handler.fromFunctionZIO[(Icon.Id, WalletContext, Request)] { (id, ctx, req) =>
    iconsService.get(ctx.wallet, id).map(base64ImageToResponse)
  }

  private def base64ImageToResponse(base64: String): Response = {
    // TODO Quick solution. Make more safe
    val contentType = base64.split(",").head.replace("data:", "").replace(";base64", "")
    val mediaType   = MediaType.forContentType(contentType)

    val base64Content = base64.split(",").last

    val bodyArray: Array[Byte] = Base64.getDecoder.decode(base64Content)
    val response               = Response(Status.Ok, body = Body.fromChunk(Chunk.fromArray(bodyArray)))

    mediaType match {
      case Some(mType) => response.addHeader(Header.ContentType(mType))
      case None        => response
    }
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
