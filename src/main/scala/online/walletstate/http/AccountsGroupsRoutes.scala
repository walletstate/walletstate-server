package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.AccountsGroup
import online.walletstate.models.api.{CreateAccountsGroup, UpdateAccountsGroup}
import online.walletstate.services.AccountsGroupsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class AccountsGroupsRoutes(auth: AuthMiddleware, accountsGroupsService: AccountsGroupsService) {

  private val createGroupHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx       <- ZIO.service[WalletContext]
      groupInfo <- req.as[CreateAccountsGroup]
      group     <- accountsGroupsService.create(ctx.wallet, groupInfo.name, groupInfo.orderingIndex, ctx.user)
    } yield Response.json(group.toJson)
  } @@ auth.ctx[WalletContext]

  private def updateGroupHandler(idStr: String) = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx        <- ZIO.service[WalletContext]
      id         <- AccountsGroup.Id.from(idStr)
      updateInfo <- req.as[UpdateAccountsGroup]
      _          <- accountsGroupsService.update(ctx.wallet, id, updateInfo.name)
    } yield Response.ok
  } @@ auth.ctx[WalletContext]

  private val getGroupsHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx    <- ZIO.service[WalletContext]
      groups <- accountsGroupsService.list(ctx.wallet)
    } yield Response.json(groups.toJson)
  } @@ auth.ctx[WalletContext]

  private def getGroupHandler(idStr: String) = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx   <- ZIO.service[WalletContext]
      id    <- AccountsGroup.Id.from(idStr)
      group <- accountsGroupsService.get(ctx.wallet, id)
    } yield Response.json(group.toJson)
  } @@ auth.ctx[WalletContext]

  def routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "groups"     => createGroupHandler
    case Method.GET -> !! / "api" / "groups"      => getGroupsHandler
    case Method.GET -> !! / "api" / "groups" / id => getGroupHandler(id)
    case Method.PUT -> !! / "api" / "groups" / id => updateGroupHandler(id)
  }
}

object AccountsGroupsRoutes {
  val layer = ZLayer.fromFunction(AccountsGroupsRoutes.apply _)
}
