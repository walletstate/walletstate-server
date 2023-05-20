package online.walletstate

import online.walletstate.config.HttpServerConfig
import online.walletstate.http.*
import online.walletstate.http.auth.{AuthMiddleware, AuthRoutesHandler, ConfiguredUsersAuthRoutesHandler}
import online.walletstate.repos.{InMemoryNamespacesRepo, InMemoryUsersRepo}
import online.walletstate.services.auth.{StatelessTokenServiceImpl, TokenService}
import online.walletstate.services.{NamespacesService, NamespacesServiceImpl, UsersServiceImpl}
import zio.*
import zio.config.typesafe.*
import zio.http.*
import zio.json.*

object Application extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())

  private val server = for {
    p <- ZIO.serviceWithZIO[WalletStateServer](s => Server.install(s.app))
    _ <- ZIO.logInfo(s"Server started on port $p")
    _ <- ZIO.never
  } yield ()

  def run = server.provide(
    HttpServerConfig.serverConfigLayer,
    Server.live,
    WalletStateServer.layer,

    // auth
    AuthRoutesHandler.layer,
    AuthMiddleware.layer,

    // routes
    HealthRoutes.layer,
    AuthRoutes.layer,
    NamespaceRoutes.layer,

    // services
    NamespacesServiceImpl.layer,
    StatelessTokenServiceImpl.layer,
    UsersServiceImpl.layer,

    // repos
    InMemoryNamespacesRepo.layer,
    InMemoryUsersRepo.layer,

    // dependencies tree
    ZLayer.Debug.mermaid
  )

}
