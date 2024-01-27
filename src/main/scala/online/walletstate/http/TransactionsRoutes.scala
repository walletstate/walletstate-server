package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models
import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.{Account, Asset, Transaction}
import online.walletstate.services.TransactionsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class TransactionsRoutes(auth: AuthMiddleware, transactionsService: TransactionsService) {

  private val createTransactionHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      info         <- req.as[CreateTransaction]
      transactions <- transactionsService.create(ctx.wallet, info)
    } yield Response.json(transactions.toJson)
  }

  private val getTransactionsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      account       <- ZIO.fromOption(req.url.queryParams.get("account")).flatMap(Account.Id.from)
      nextPageToken <- Transaction.Page.Token.from(req.url.queryParams.get("page"))
      transactions  <- transactionsService.list(ctx.wallet, account, nextPageToken)
    } yield Response.json(transactions.toJson)
  }

  private val getTransactionHandler = Handler.fromFunctionZIO[(Transaction.Id, WalletContext, Request)] {
    (id, ctx, req) =>
      for {
        transactions <- transactionsService.get(ctx.wallet, id)
      } yield Response.json(transactions.toJson)
  }

  def routes = Routes(
    Method.POST / "api" / "transactions"                      -> auth.walletCtx -> createTransactionHandler,
    Method.GET / "api" / "transactions"                       -> auth.walletCtx -> getTransactionsHandler,
    Method.GET / "api" / "transactions" / Transaction.Id.path -> auth.walletCtx -> getTransactionHandler
  )
}

object TransactionsRoutes {
  val layer = ZLayer.fromFunction(TransactionsRoutes.apply _)
}
