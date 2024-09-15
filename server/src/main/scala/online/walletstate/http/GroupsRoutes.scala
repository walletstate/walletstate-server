package online.walletstate.http

import online.walletstate.http.endpoints.GroupsEndpoints
import online.walletstate.services.GroupsService
import zio.*
import zio.http.*

case class GroupsRoutes(groupsService: GroupsService) extends WalletStateRoutes with GroupsEndpoints {

  private val createRoute =
    createEndpoint.implement(groupInfo => groupsService.create(groupInfo.`type`, groupInfo.name, groupInfo.idx))
  private val listRoute = listEndpoint.implement(`type` => groupsService.list(`type`))
  private val getRoute  = getEndpoint.implement(id => groupsService.get(id).mapError(_.asNotFound))
  private val updateRoute =
    updateEndpoint.implement((id, updateInfo) => groupsService.update(id, updateInfo.name, updateInfo.idx))
  private val deleteRoute = deleteEndpoint.implement(id => groupsService.delete(id))

  override val walletRoutes = Routes(createRoute, listRoute, getRoute, updateRoute, deleteRoute)
}

object GroupsRoutes {
  val layer = ZLayer.fromFunction(GroupsRoutes.apply _)
}
