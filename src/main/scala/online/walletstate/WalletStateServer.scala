package online.walletstate

import online.walletstate.db.Migrations
import online.walletstate.http.*
import online.walletstate.models.AppError
import online.walletstate.utils.RequestOps
import zio.*
import zio.http.*
import zio.http.codec.HttpCodecError
import zio.http.endpoint.Endpoint
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}

final case class WalletStateServer(
    health: HealthRoutes,
    auth: AuthRoutes,
    wallets: WalletsRoutes,
    groupsRoutes: GroupsRoutes,
    accounts: AccountsRoutes,
    categories: CategoriesRoutes,
    assets: AssetsRoutes,
    exchangeRates: ExchangeRatesRoutes,
    records: RecordsRoutes,
    analytics: AnalyticsRoutes,
    icons: IconsRoutes,
    migrations: Migrations
) {

  private val openAPISpec = OpenAPIGen.fromEndpoints(
    title = "WalletState.online API",
    version = "0.0.1",
    accounts.endpoints ++ assets.endpoints ++ categories.endpoints ++
      exchangeRates.endpoints ++ groupsRoutes.endpoints ++ records.endpoints ++
      analytics.endpoints ++ wallets.endpoints ++ icons.endpoints
  )

  private val routes =
    health.routes ++
      auth.routes ++
      wallets.routes ++
      groupsRoutes.routes ++
      accounts.routes ++
      categories.routes ++
      assets.routes ++
      exchangeRates.routes ++
      records.routes ++
      analytics.routes ++
      icons.routes ++
      SwaggerUI.routes(Endpoint(Method.GET / "api" / "docs").route.pathCodec, openAPISpec)

  def app = routes.handleError(e => Response.error(Status.InternalServerError)) @@ Middleware.requestLogging()

  def start = for {
    _ <- migrations.migrate
    p <- Server.install(app)
    _ <- ZIO.logInfo(s"Server started on port $p")
    _ <- ZIO.never
  } yield ()
}

object WalletStateServer {
  val layer = ZLayer.fromFunction(WalletStateServer.apply _)
}
