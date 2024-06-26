package online.walletstate.http

import online.walletstate.http.endpoints.CategoriesEndpoints
import online.walletstate.services.CategoriesService
import zio.*
import zio.http.*

case class CategoriesRoutes(categoriesService: CategoriesService) extends WalletStateRoutes with CategoriesEndpoints {

  private val createRoute = createEndpoint.implement {
    Handler.fromFunctionZIO(info => categoriesService.create(info))
  }

  private val listRoute = listEndpoint.implement {
    Handler.fromFunctionZIO(_ => categoriesService.list)
  }

  private val listGroupedRoute = listGroupedEndpoint.implement {
    Handler.fromFunctionZIO(_ => categoriesService.grouped)
  }

  private val getRoute = getEndpoint.implement {
    Handler.fromFunctionZIO(id => categoriesService.get(id).mapError(_.asNotFound))
  }

  private val updateRoute = updateEndpoint.implement {
    Handler.fromFunctionZIO((id, info) => categoriesService.update(id, info))
  }

  override val walletRoutes = Routes(createRoute, listRoute, listGroupedRoute, getRoute, updateRoute)
}

object CategoriesRoutes {
  val layer = ZLayer.fromFunction(CategoriesRoutes.apply _)
}
