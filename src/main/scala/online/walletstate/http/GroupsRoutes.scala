package online.walletstate.http

import online.walletstate.http.api.GroupsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Group
import online.walletstate.models.api.{CreateGroup, UpdateGroup}
import online.walletstate.services.GroupsService
import zio.*
import zio.http.*

case class GroupsRoutes(auth: AuthMiddleware, groupsService: GroupsService) extends GroupsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(CreateGroup, WalletContext)] {
    Handler.fromFunctionZIO((groupInfo, ctx) =>
      groupsService.create(ctx.wallet, groupInfo.`type`, groupInfo.name, groupInfo.idx, ctx.user)
    )
  }()

  private val listRoute = list.implementWithWalletCtx[(Group.Type, WalletContext)] {
    Handler.fromFunctionZIO((`type`, ctx) => groupsService.list(ctx.wallet, `type`))
  }()

  private val getRoute = get.implementWithWalletCtx[(Group.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => groupsService.get(ctx.wallet, id))
  }()

  private val updateRoute = update.implementWithWalletCtx[(Group.Id, UpdateGroup, WalletContext)] {
    Handler.fromFunctionZIO((id, updateInfo, ctx) =>
      groupsService.update(ctx.wallet, id, updateInfo.name, updateInfo.idx)
    )
  }()

  private val deleteRoute = delete.implementWithWalletCtx[(Group.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => groupsService.delete(ctx.wallet, id))
  }()

  val routes = Routes(createRoute, listRoute, getRoute, updateRoute, deleteRoute)
}

object GroupsRoutes {
  val layer = ZLayer.fromFunction(GroupsRoutes.apply _)
}
