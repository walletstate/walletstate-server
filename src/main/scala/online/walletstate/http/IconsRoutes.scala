package online.walletstate.http

import online.walletstate.http.endpoints.IconsEndpoints
import online.walletstate.models.{AppError, Icon}
import online.walletstate.services.IconsService
import online.walletstate.utils.RequestOps.outputMediaType
import zio.http.*
import zio.{Chunk, Task, ZIO, ZLayer}

import java.util.Base64

case class IconsRoutes(iconsService: IconsService) extends WalletStateRoutes with IconsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(info => iconsService.create(info).map(_.id))
  }

  private val listRoute = list.implement {
    Handler.fromFunctionZIO(maybeTag => iconsService.listIds(maybeTag))
  }

  private val getIconHandler = Handler.fromFunctionZIO[(Icon.Id, Request)] { (id, req) =>
    iconsService.get(id).flatMap(iconToResponse).catchAll {
      case e: AppError.IconNotExist => e.encode(Status.NotFound, req.outputMediaType)
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

  override val walletRoutes = Routes(
    createRoute,
    listRoute,
    Method.GET / "api" / "icons" / Icon.Id.path -> getIconHandler
  )

}

object IconsRoutes {
  val layer = ZLayer.fromFunction(IconsRoutes.apply _)
}
