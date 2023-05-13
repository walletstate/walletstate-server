package online.walletstate

import online.walletstate.config.{AppConfig, HttpServerConfig}
import online.walletstate.domain.Namespace
import online.walletstate.repos.ImMemoryNamespaceRepo
import online.walletstate.http.{HealthRoutes, Routes}
import online.walletstate.services.{NamespaceService, NamespaceServiceImpl}
import zio.http.*
import zio.*
import zio.config.typesafe.*

import java.net.InetSocketAddress
import java.util.UUID

object Application extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())

  val app =
    new Routes().routes
      .mapError(e => Response(status = Status.InternalServerError, body = Body.fromString(e.getMessage)))

  def run = Server
    .serve(app)
    .flatMap(_ => Console.printLine("Server started"))
    .provide(
      HttpServerConfig.serverConfigLayer,
      Server.live,
      // namespace
      NamespaceServiceImpl.layer,
      ImMemoryNamespaceRepo.layer
    )

}
