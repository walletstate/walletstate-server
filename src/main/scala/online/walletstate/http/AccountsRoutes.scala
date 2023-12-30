package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Account
import online.walletstate.models.api.CreateAccount
import online.walletstate.services.AccountsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class AccountsRoutes(auth: AuthMiddleware, accountsService: AccountsService) {

  private val createAccountHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      accInfo <- req.as[CreateAccount] // TODO validate group is in current user wallet
      account <- accountsService.create(
        accInfo.group,
        accInfo.name,
        accInfo.orderingIndex,
        accInfo.icon,
        accInfo.tags,
        ctx.user
      )
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

  def routes = Routes(
    Method.POST / "api" / "accounts"                  -> auth.walletCtx -> createAccountHandler,
    Method.GET / "api" / "accounts" / "grouped"       -> auth.walletCtx -> getGroupedAccountsHandler,
    Method.GET / "api" / "accounts"                   -> auth.walletCtx -> getAccountsHandler,
    Method.GET / "api" / "accounts" / Account.Id.path -> auth.walletCtx -> getAccountHandler
  )
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
