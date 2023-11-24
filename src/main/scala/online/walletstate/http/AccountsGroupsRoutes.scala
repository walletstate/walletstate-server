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

  private val createGroupHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      groupInfo <- req.as[CreateAccountsGroup]
      group     <- accountsGroupsService.create(ctx.wallet, groupInfo.name, groupInfo.orderingIndex, ctx.user)
    } yield Response.json(group.toJson)
  }

  private val updateGroupHandler = Handler.fromFunctionZIO[(AccountsGroup.Id, WalletContext, Request)] {
    (id, ctx, req) =>
      for {
        updateInfo <- req.as[UpdateAccountsGroup]
        _          <- accountsGroupsService.update(ctx.wallet, id, updateInfo.name)
      } yield Response.ok
  }

  private val deleteGroupHandler = Handler.fromFunctionZIO[(AccountsGroup.Id, WalletContext, Request)] {
    (id, ctx, req) =>
      for {
        _ <- accountsGroupsService.delete(ctx.wallet, id)
      } yield Response.ok
  }

  private val getGroupsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      groups <- accountsGroupsService.list(ctx.wallet)
    } yield Response.json(groups.toJson)
  }

  private val getGroupHandler = Handler.fromFunctionZIO[(AccountsGroup.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      group <- accountsGroupsService.get(ctx.wallet, id)
    } yield Response.json(group.toJson)
  }

  private val getGroupsWithAccountsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      groups <- accountsGroupsService.listWithAccounts(ctx.wallet)
    } yield Response.json(groups.toJson)
  }

  def routes = Routes(
    Method.POST / "api" / "groups"                           -> auth.walletCtx -> createGroupHandler,
    Method.GET / "api" / "groups"                            -> auth.walletCtx -> getGroupsHandler,
    Method.GET / "api" / "groups" / "with-accounts"          -> auth.walletCtx -> getGroupsWithAccountsHandler,
    Method.GET / "api" / "groups" / AccountsGroup.Id.path    -> auth.walletCtx -> getGroupHandler,
    Method.PUT / "api" / "groups" / AccountsGroup.Id.path    -> auth.walletCtx -> updateGroupHandler,
    Method.DELETE / "api" / "groups" / AccountsGroup.Id.path -> auth.walletCtx -> deleteGroupHandler
  )
}

object AccountsGroupsRoutes {
  val layer = ZLayer.fromFunction(AccountsGroupsRoutes.apply _)
}
