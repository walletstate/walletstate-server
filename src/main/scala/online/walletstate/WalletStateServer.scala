package online.walletstate

import online.walletstate.db.Migrations
import online.walletstate.http.*
import online.walletstate.http.auth.AuthMiddleware
import online.walletstate.http.endpoints.WalletStateEndpoints
import online.walletstate.models.AppError
import zio.*
import zio.http.*
import zio.http.endpoint.Endpoint
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}

final case class WalletStateServer(
    authMiddleware: AuthMiddleware,
    health: HealthRoutes,
    auth: AuthRoutes,
    wallets: WalletsRoutes,
    groups: GroupsRoutes,
    accounts: AccountsRoutes,
    categories: CategoriesRoutes,
    assets: AssetsRoutes,
    exchangeRates: ExchangeRatesRoutes,
    records: RecordsRoutes,
    analytics: AnalyticsRoutes,
    icons: IconsRoutes,
    migrations: Migrations
) {

  private val routesClasses: Chunk[WalletStateRoutes with WalletStateEndpoints] =
    Chunk(health, auth, wallets, groups, accounts, categories, assets, exchangeRates, records, analytics, icons)

  private val endpoints =
    routesClasses
      .flatMap(_.endpoints)
      .map(_.outError[AppError.Unauthorized](Status.Unauthorized))

  private val openAPISpec = OpenAPIGen.fromEndpoints(
    title = "WalletState.online API",
    version = "0.0.1",
    endpoints = endpoints
  )

  private val swaggerRoutes = SwaggerUI.routes(Endpoint(Method.GET / "api" / "docs").route.pathCodec, openAPISpec)

  private val noCtxRoutes  = routesClasses.map(_.noCtxRoutes).fold(Routes.empty)(_ ++ _)
  private val userRoutes   = routesClasses.map(_.userRoutes).fold(Routes.empty)(_ ++ _) @@ authMiddleware.userCtx
  private val walletRoutes = routesClasses.map(_.walletRoutes).fold(Routes.empty)(_ ++ _) @@ authMiddleware.walletCtx

  private val routes = noCtxRoutes ++ userRoutes ++ walletRoutes ++ swaggerRoutes

  private val app = routes.handleError(e => Response.error(Status.InternalServerError)) @@ Middleware.requestLogging()

  val start = for {
    _ <- migrations.migrate
    p <- Server.install(app)
    _ <- ZIO.logInfo(s"Server started on port $p")
    _ <- ZIO.never
  } yield ()
}

object WalletStateServer {
  val layer = ZLayer.fromFunction(WalletStateServer.apply _)
}
