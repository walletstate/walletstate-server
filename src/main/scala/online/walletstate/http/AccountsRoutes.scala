package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, UserNamespaceContext}
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
      ctx     <- ZIO.service[UserNamespaceContext]
      accInfo <- req.as[CreateAccount]
      account <- accountsService.create(ctx.namespace, accInfo.name, ctx.user)
    } yield Response.json(account.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private val getAccountsHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[UserNamespaceContext]
      accounts <- accountsService.list(ctx.namespace)
    } yield Response.json(accounts.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private def getAccountHandler(idStr: String) = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx     <- ZIO.service[UserNamespaceContext]
      id      <- Account.Id.from(idStr)
      account <- accountsService.get(ctx.namespace, id)
    } yield Response.json(account.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  def routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "accounts"     => createAccountHandler
    case Method.GET -> !! / "api" / "accounts"      => getAccountsHandler
    case Method.GET -> !! / "api" / "accounts" / id => getAccountHandler(id)
  }
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
