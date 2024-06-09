package online.walletstate.http

import online.walletstate.http.endpoints.GroupsEndpoints
import online.walletstate.models.HttpError
import online.walletstate.services.GroupsService
import zio.*
import zio.http.*

case class GroupsRoutes(groupsService: GroupsService) extends WalletStateRoutes with GroupsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(groupInfo => groupsService.create(groupInfo.`type`, groupInfo.name, groupInfo.idx))
  }

  private val listRoute = list.implement {
    Handler.fromFunctionZIO(`type` => groupsService.list(`type`))
  }

  private val getRoute = get.implement {
    Handler.fromFunctionZIO(id => groupsService.get(id).mapError(HttpError.NotFound.apply))
  }

  private val updateRoute = update.implement {
    Handler.fromFunctionZIO((id, updateInfo) => groupsService.update(id, updateInfo.name, updateInfo.idx))
  }

  private val deleteRoute = delete.implement {
    Handler.fromFunctionZIO(id => groupsService.delete(id))
  }

  override val walletRoutes = Routes(createRoute, listRoute, getRoute, updateRoute, deleteRoute)
}

object GroupsRoutes {
  val layer = ZLayer.fromFunction(GroupsRoutes.apply _)
}
