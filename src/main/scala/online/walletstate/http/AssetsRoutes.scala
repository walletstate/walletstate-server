package online.walletstate.http

import online.walletstate.http.endpoints.AssetsEndpoints
import online.walletstate.services.AssetsService
import zio.ZLayer
import zio.http.*

final case class AssetsRoutes(assetsService: AssetsService) extends WalletStateRoutes with AssetsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(info => assetsService.create(info))
  }

  private val listRoute = list.implement {
    Handler.fromFunctionZIO(_ => assetsService.list)
  }

  private val listGroupedRoute = listGrouped.implement {
    Handler.fromFunctionZIO(_ => assetsService.grouped)
  }

  private val getRoute = get.implement {
    Handler.fromFunctionZIO(id => assetsService.get(id))
  }

  private val updateRoute = update.implement {
    Handler.fromFunctionZIO((id, info) => assetsService.update(id, info))
  }

  override val walletRoutes = Routes(createRoute, listGroupedRoute, listRoute, getRoute, updateRoute)
}

object AssetsRoutes {
  val layer = ZLayer.fromFunction(AssetsRoutes.apply _)
}
