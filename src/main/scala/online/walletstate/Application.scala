package online.walletstate

import online.walletstate.config.{AppConfig, HttpServerConfig}
import online.walletstate.domain.Namespace
import online.walletstate.domain.errors.AppHttpError
import online.walletstate.http.auth.{AuthMiddleware, AuthRoutesHandler, ConfiguredUsersAuthRoutesHandler}
import online.walletstate.http.*
import online.walletstate.repos.{InMemoryNamespaceRepo, InMemoryUsersRepo}
import online.walletstate.services.auth.{StatelessTokenServiceImpl, TokenService}
import online.walletstate.services.{NamespaceService, NamespaceServiceImpl, UsersServiceImpl}
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
    NamespaceServiceImpl.layer,
    StatelessTokenServiceImpl.layer,
    UsersServiceImpl.layer,

    // repos
    InMemoryNamespaceRepo.layer,
    InMemoryUsersRepo.layer,

    // dependencies tree
    ZLayer.Debug.mermaid
  )

}
