package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Group
import online.walletstate.models.api.{CreateGroup, UpdateAccountsGroup}
import online.walletstate.services.GroupsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class GroupsRoutes(auth: AuthMiddleware, accountsGroupsService: GroupsService) {

  private val createGroupHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      groupInfo <- req.as[CreateGroup]
      group     <- accountsGroupsService.create(ctx.wallet, groupInfo.name, groupInfo.orderingIndex, ctx.user)
    } yield Response.json(group.toJson)
  }

  private val updateGroupHandler = Handler.fromFunctionZIO[(Group.Id, WalletContext, Request)] {
    (id, ctx, req) =>
      for {
        updateInfo <- req.as[UpdateAccountsGroup]
        _          <- accountsGroupsService.update(ctx.wallet, id, updateInfo.name)
      } yield Response.ok
  }

  private val deleteGroupHandler = Handler.fromFunctionZIO[(Group.Id, WalletContext, Request)] {
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

  private val getGroupHandler = Handler.fromFunctionZIO[(Group.Id, WalletContext, Request)] { (id, ctx, req) =>
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
    Method.GET / "api" / "groups" / Group.Id.path    -> auth.walletCtx -> getGroupHandler,
    Method.PUT / "api" / "groups" / Group.Id.path    -> auth.walletCtx -> updateGroupHandler,
    Method.DELETE / "api" / "groups" / Group.Id.path -> auth.walletCtx -> deleteGroupHandler
  )
}

object GroupsRoutes {
  val layer = ZLayer.fromFunction(GroupsRoutes.apply _)
}
