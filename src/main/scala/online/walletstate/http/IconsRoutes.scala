package online.walletstate.http

import online.walletstate.http.api.IconsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.{AppError, Icon}
import online.walletstate.models.api.CreateIcon
import online.walletstate.services.IconsService
import online.walletstate.utils.RequestOps.outputMediaType
import zio.{Chunk, Task, ZIO, ZLayer}
import zio.http.*

import java.util.Base64

case class IconsRoutes(auth: AuthMiddleware, iconsService: IconsService) extends IconsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(CreateIcon, WalletContext)] {
    Handler.fromFunctionZIO((info, ctx) => iconsService.create(ctx.wallet, info).map(_.id))
  }()

  private val listRoute = list.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => iconsService.listIds(ctx.wallet))
  }()

  private val getIconHandler = Handler.fromFunctionZIO[(Icon.Id, WalletContext, Request)] { (id, ctx, req) =>
    iconsService.get(ctx.wallet, id).flatMap(iconToResponse).catchAll {
      case e: AppError.IconNotFount => e.encode(Status.NotFound, req.outputMediaType)
      case _ => AppError.InternalServerError.encode(Status.InternalServerError, req.outputMediaType)
    }
  }

  // TODO add cache header
  private def iconToResponse(icon: Icon): Task[Response] = ZIO.attempt {
    val mediaType              = MediaType.forContentType(icon.contentType)
    val bodyArray: Array[Byte] = Base64.getDecoder.decode(icon.content)
    val response               = Response(Status.Ok, body = Body.fromChunk(Chunk.fromArray(bodyArray)))

    mediaType match {
      case Some(mType) => response.addHeader(Header.ContentType(mType))
      case None        => response
    }
  }

  val routes = Routes(
    createRoute,
    listRoute,
    Method.GET / "api" / "icons" / Icon.Id.path -> auth.walletCtx -> getIconHandler
  )

}

object IconsRoutes {
  val layer = ZLayer.fromFunction(IconsRoutes.apply _)
}
