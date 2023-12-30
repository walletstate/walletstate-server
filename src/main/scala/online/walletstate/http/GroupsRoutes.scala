package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Group
import online.walletstate.models.api.{CreateGroup, UpdateGroup}
import online.walletstate.services.GroupsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class GroupsRoutes(auth: AuthMiddleware, accountsGroupsService: GroupsService) {

  private val createGroupHandler = Handler.fromFunctionZIO[(Group.Type, WalletContext, Request)] { (`type`, ctx, req) =>
    for {
      groupInfo <- req.as[CreateGroup]
      group     <- accountsGroupsService.create(ctx.wallet, `type`, groupInfo.name, groupInfo.orderingIndex, ctx.user)
    } yield Response.json(group.toJson)
  }

  private val updateGroupHandler = Handler.fromFunctionZIO[(Group.Type, Group.Id, WalletContext, Request)] {
    (`type`, id, ctx, req) =>
      for {
        updateInfo <- req.as[UpdateGroup]
        _          <- accountsGroupsService.update(ctx.wallet, `type`, id, updateInfo.name)
      } yield Response.ok
  }

  private val deleteGroupHandler = Handler.fromFunctionZIO[(Group.Type, Group.Id, WalletContext, Request)] {
    (`type`, id, ctx, req) =>
      for {
        _ <- accountsGroupsService.delete(ctx.wallet, `type`, id)
      } yield Response.ok
  }

  private val getGroupsHandler = Handler.fromFunctionZIO[(Group.Type, WalletContext, Request)] { (`type`, ctx, req) =>
    for {
      groups <- accountsGroupsService.list(ctx.wallet, `type`)
    } yield Response.json(groups.toJson)
  }

  private val getGroupHandler = Handler.fromFunctionZIO[(Group.Type, Group.Id, WalletContext, Request)] {
    (`type`, id, ctx, req) =>
      for {
        group <- accountsGroupsService.get(ctx.wallet, `type`, id)
      } yield Response.json(group.toJson)
  }

  val routes = Routes(
    Method.POST / "api" / "groups" / Group.Type.path                   -> auth.walletCtx -> createGroupHandler,
    Method.GET / "api" / "groups" / Group.Type.path                    -> auth.walletCtx -> getGroupsHandler,
    Method.GET / "api" / "groups" / Group.Type.path / Group.Id.path    -> auth.walletCtx -> getGroupHandler,
    Method.PUT / "api" / "groups" / Group.Type.path / Group.Id.path    -> auth.walletCtx -> updateGroupHandler,
    Method.DELETE / "api" / "groups" / Group.Type.path / Group.Id.path -> auth.walletCtx -> deleteGroupHandler
  )
}

object GroupsRoutes {
  val layer = ZLayer.fromFunction(GroupsRoutes.apply _)
}
