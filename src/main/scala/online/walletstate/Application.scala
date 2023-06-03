package online.walletstate

import io.getquill.jdbczio.Quill
import online.walletstate.config.HttpServerConfig
import online.walletstate.http.*
import online.walletstate.http.auth.{AuthMiddleware, AuthRoutesHandler, ConfiguredUsersAuthRoutesHandler}
import online.walletstate.services.auth.{StatelessTokenService, TokenService}
import online.walletstate.models.db.QuillNamingStrategy
import online.walletstate.services.{NamespaceInvitesServiceLive, NamespacesService, NamespacesServiceLive, UsersServiceLive}
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
    NamespacesServiceLive.layer,
    NamespaceInvitesServiceLive.layer,
    UsersServiceLive.layer,
    StatelessTokenService.layer,

    // DB
    Quill.Postgres.fromNamingStrategy(QuillNamingStrategy),
    Quill.DataSource.fromPrefix("db"),

    // dependencies tree
    ZLayer.Debug.mermaid
  )

}
