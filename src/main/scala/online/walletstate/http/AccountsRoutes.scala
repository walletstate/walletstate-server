package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.{Account, Transaction}
import online.walletstate.models.api.CreateAccount
import online.walletstate.services.{AccountsService, TransactionsService}
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class AccountsRoutes(
    auth: AuthMiddleware,
    accountsService: AccountsService,
    transactionsService: TransactionsService
) {

  private val createAccountHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      accInfo <- req.as[CreateAccount]
      account <- accountsService.create(ctx.wallet, ctx.user, accInfo)
    } yield Response.json(account.toJson)
  }

  private val getAccountsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      accounts <- accountsService.list(ctx.wallet)
    } yield Response.json(accounts.toJson)
  }

  private val getGroupedAccountsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      accounts <- accountsService.grouped(ctx.wallet)
    } yield Response.json(accounts.toJson)
  }

  private val getAccountHandler = Handler.fromFunctionZIO[(Account.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      account <- accountsService.get(ctx.wallet, id)
    } yield Response.json(account.toJson)
  }

  private val getTransactionsHandler = Handler.fromFunctionZIO[(Account.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      nextPageToken <- Transaction.Page.Token.from(req.url.queryParams.queryParam("page"))
      transactions  <- transactionsService.list(ctx.wallet, id, nextPageToken)
    } yield Response.json(transactions.toJson)
  }

  private val getBalanceHandler = Handler.fromFunctionZIO[(Account.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      balance <- transactionsService.balance(ctx.wallet, id)
    } yield Response.json(balance.toJson)
  }

  def routes = Routes(
    Method.POST / "api" / "accounts"                                   -> auth.walletCtx -> createAccountHandler,
    Method.GET / "api" / "accounts" / "grouped"                        -> auth.walletCtx -> getGroupedAccountsHandler,
    Method.GET / "api" / "accounts"                                    -> auth.walletCtx -> getAccountsHandler,
    Method.GET / "api" / "accounts" / Account.Id.path                  -> auth.walletCtx -> getAccountHandler,
    Method.GET / "api" / "accounts" / Account.Id.path / "transactions" -> auth.walletCtx -> getTransactionsHandler,
    Method.GET / "api" / "accounts" / Account.Id.path / "balance"      -> auth.walletCtx -> getBalanceHandler
  )
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
