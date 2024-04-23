package online.walletstate.http

import online.walletstate.http.api.endpoints.TransactionsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models
import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.{Account, Asset, Transaction}
import online.walletstate.services.TransactionsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class TransactionsRoutes(auth: AuthMiddleware, transactionsService: TransactionsService)
    extends TransactionsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(CreateTransaction, WalletContext)] {
    Handler.fromFunctionZIO((info, ctx) => transactionsService.create(ctx.wallet, info).map(Chunk.from))
  }()

  private val listRoute = list.implementWithWalletCtx[(Account.Id, Option[Transaction.Page.Token], WalletContext)] {
    Handler.fromFunctionZIO((account, nextPageToken, ctx) =>
      transactionsService.list(ctx.wallet, account, nextPageToken)
    )
  }()

  private val getRoute = get.implementWithWalletCtx[(Transaction.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => transactionsService.get(ctx.wallet, id).map(Chunk.from))
  }()

  def routes = Routes(createRoute, listRoute, getRoute)
}

object TransactionsRoutes {
  val layer = ZLayer.fromFunction(TransactionsRoutes.apply _)
}
