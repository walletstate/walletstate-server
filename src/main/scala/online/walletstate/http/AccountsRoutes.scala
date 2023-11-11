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

  private val createAccountHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx     <- ZIO.service[WalletContext]
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
  } @@ auth.ctx[WalletContext]

  private val getAccountsHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[WalletContext]
      accounts <- accountsService.list(ctx.wallet)
    } yield Response.json(accounts.toJson)
  } @@ auth.ctx[WalletContext]

  private def getAccountHandler(idStr: String) = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx     <- ZIO.service[WalletContext]
      id      <- Account.Id.from(idStr)
      account <- accountsService.get(ctx.wallet, id)
    } yield Response.json(account.toJson)
  } @@ auth.ctx[WalletContext]

  def routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "accounts"     => createAccountHandler
    case Method.GET -> !! / "api" / "accounts"      => getAccountsHandler
    case Method.GET -> !! / "api" / "accounts" / id => getAccountHandler(id)
  }
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
