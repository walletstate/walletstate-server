package online.walletstate.http

import online.walletstate.config.AppConfig
import online.walletstate.domain.errors.AppHttpError
import zio.*
import zio.http.*
import zio.json.*

final case class WalletStateServer(health: HealthRoutes, auth: AuthRoutes, namespace: NamespaceRoutes) {

  private val routes = health.routes ++ auth.routes ++ namespace.routes

  private val errorHandler: Any => Response = {
    case e: ParseError =>
      Response(status = Status.BadRequest, body = Body.fromString(e.toJson))
    case AppHttpError(status, msg) =>
      Response(status, body = Body.fromString(msg))
    case e: Throwable =>
      Response(status = Status.InternalServerError, body = Body.fromString(e.getMessage))
    case e =>
      Response(status = Status.InternalServerError, body = Body.fromString(e.toString))
  }

  def app = routes.mapError(errorHandler) @@ RequestHandlerMiddlewares.requestLogging()
}

object WalletStateServer {
  val layer: ZLayer[HealthRoutes with AuthRoutes with NamespaceRoutes, Nothing, WalletStateServer] =
    ZLayer.fromFunction(WalletStateServer.apply _)
}
