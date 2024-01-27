package online.walletstate

import online.walletstate.db.Migrations
import online.walletstate.http.*
import online.walletstate.models.errors.ToResponse
import zio.*
import zio.http.*
import zio.json.*

final case class WalletStateServer(
    health: HealthRoutes,
    auth: AuthRoutes,
    wallets: WalletsRoutes,
    accountsGroupsRoutes: GroupsRoutes,
    accounts: AccountsRoutes,
    categories: CategoriesRoutes,
    assets: AssetsRoutes,
    exchangeRates: ExchangeRatesRoutes,
    transactions: TransactionsRoutes,
    icons: IconsRoutes,
    migrations: Migrations
) {

  private val routes =
    health.routes ++
      auth.routes ++
      wallets.routes ++
      accountsGroupsRoutes.routes ++
      accounts.routes ++
      categories.routes ++
      assets.routes ++
      exchangeRates.routes ++
      transactions.routes ++
      icons.routes

  def app = routes.handleError { // TODO Investigate what was changed
    case e: ToResponse => e.toResponse
    case e             => Response.error(Status.InternalServerError) // .debug(s"Error ${e.toString}")
  } @@ Middleware.requestLogging()

  def start = for {
    _ <- migrations.migrate
    p <- Server.install(app.toHttpApp)
    _ <- ZIO.logInfo(s"Server started on port $p")
    _ <- ZIO.never
  } yield ()
}

object WalletStateServer {
  val layer = ZLayer.fromFunction(WalletStateServer.apply _)
}
