package online.walletstate.http

import online.walletstate.http.endpoints.CategoriesEndpoints
import online.walletstate.services.CategoriesService
import zio.*
import zio.http.*

case class CategoriesRoutes(categoriesService: CategoriesService) extends WalletStateRoutes with CategoriesEndpoints {

  private val createRoute      = createEndpoint.implement(info => categoriesService.create(info))
  private val listRoute        = listEndpoint.implement(_ => categoriesService.list)
  private val listGroupedRoute = listGroupedEndpoint.implement(_ => categoriesService.grouped)
  private val getRoute         = getEndpoint.implement(id => categoriesService.get(id).mapError(_.asNotFound))
  private val updateRoute      = updateEndpoint.implement((id, info) => categoriesService.update(id, info))

  override val walletRoutes = Routes(createRoute, listRoute, listGroupedRoute, getRoute, updateRoute)
}

object CategoriesRoutes {
  val layer = ZLayer.fromFunction(CategoriesRoutes.apply _)
}
