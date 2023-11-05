package online.walletstate

import io.getquill.jdbczio.Quill
import online.walletstate.config.HttpServerConfig
import online.walletstate.db.{Migrations, QuillNamingStrategy}
import online.walletstate.http.*
import online.walletstate.http.auth.{AuthMiddleware, AuthRoutesHandler, ConfiguredUsersAuthRoutesHandler}
import online.walletstate.services.*
import zio.*
import zio.config.typesafe.*
import zio.http.*
import zio.json.*
import zio.logging.backend.SLF4J
import zio.logging.removeDefaultLoggers

object Application extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath()) ++
      Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  def run =
    ZIO
      .serviceWithZIO[WalletStateServer](_.start)
      .provide(
        HttpServerConfig.serverConfigLayer,
        Server.live,
        WalletStateServer.layer,

        // auth
        AuthRoutesHandler.layer,
        AuthMiddleware.layer,

        // routes
        HealthRoutes.layer,
        AuthRoutes.layer,
        WalletsRoutes.layer,
        AccountsGroupsRoutes.layer,
        AccountsRoutes.layer,
        CategoriesRoutes.layer,
        RecordsRoutes.layer,

        // services
        WalletsServiceLive.layer,
        WalletInvitesServiceLive.layer,
        UsersServiceLive.layer,
        StatelessTokenService.layer,
        AccountsGroupsServiceLive.layer,
        AccountsServiceLive.layer,
        CategoriesServiceLive.layer,
        RecordsServiceLive.layer,

        // DB
        Quill.Postgres.fromNamingStrategy(QuillNamingStrategy),
        Quill.DataSource.fromPrefix("db"),
        Migrations.layer,

        // dependencies tree
        ZLayer.Debug.mermaid
      )

}
