package online.walletstate.http

import online.walletstate.domain.errors.ToResponse
import zio.*
import zio.http.*
import zio.json.*

final case class WalletStateServer(health: HealthRoutes, auth: AuthRoutes, namespace: NamespaceRoutes) {

  private val routes = health.routes ++ auth.routes ++ namespace.routes

  def app =
    routes.catchAllZIO {
      case e: ToResponse => ZIO.succeed(e.toResponse)
      case e             => ZIO.fail(e).debug(s"Error ${e.toString}")
    }.withDefaultErrorResponse @@ RequestHandlerMiddlewares.requestLogging()
}

object WalletStateServer {
  val layer: ZLayer[HealthRoutes with AuthRoutes with NamespaceRoutes, Nothing, WalletStateServer] =
    ZLayer.fromFunction(WalletStateServer.apply _)
}
