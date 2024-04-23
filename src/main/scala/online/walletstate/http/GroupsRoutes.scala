package online.walletstate.http

import online.walletstate.http.api.endpoints.GroupsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Group
import online.walletstate.models.api.{CreateGroup, UpdateGroup}
import online.walletstate.services.GroupsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class GroupsRoutes(auth: AuthMiddleware, groupsService: GroupsService) extends GroupsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(Group.Type, CreateGroup, WalletContext)] {
    Handler.fromFunctionZIO((`type`, groupInfo, ctx) =>
      groupsService.create(ctx.wallet, `type`, groupInfo.name, groupInfo.orderingIndex, ctx.user)
    )
  }()

  private val listRoute = list.implementWithWalletCtx[(Group.Type, WalletContext)] {
    Handler.fromFunctionZIO((`type`, ctx) => groupsService.list(ctx.wallet, `type`).map(Chunk.from))
  }()

  private val getRoute = get.implementWithWalletCtx[(Group.Type, Group.Id, WalletContext)] {
    Handler.fromFunctionZIO((`type`, id, ctx) => groupsService.get(ctx.wallet, `type`, id))
  }()

  private val updateRoute = update.implementWithWalletCtx[(Group.Type, Group.Id, UpdateGroup, WalletContext)] {
    Handler.fromFunctionZIO((`type`, id, updateInfo, ctx) =>
      groupsService.update(ctx.wallet, `type`, id, updateInfo.name, updateInfo.orderingIndex)
    )
  }()

  private val deleteRoute = delete.implementWithWalletCtx[(Group.Type, Group.Id, WalletContext)] {
    Handler.fromFunctionZIO((`type`, id, ctx) => groupsService.delete(ctx.wallet, `type`, id))
  }()

  val routes = Routes(createRoute, listRoute, getRoute, updateRoute, deleteRoute)
}

object GroupsRoutes {
  val layer = ZLayer.fromFunction(GroupsRoutes.apply _)
}
