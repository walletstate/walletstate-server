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
    accounts: AccountsRoutes,
    categories: CategoriesRoutes,
    records: RecordsRoutes,
    migrations: Migrations
) {

  private val routes =
    health.routes ++
      auth.routes ++
      wallets.routes ++
      accounts.routes ++
      categories.routes ++
      records.routes

  def app = routes.catchAllZIO {
    case e: ToResponse => ZIO.succeed(e.toResponse)
    case e             => ZIO.fail(e).debug(s"Error ${e.toString}")
  }.withDefaultErrorResponse @@ RequestHandlerMiddlewares.requestLogging()

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
