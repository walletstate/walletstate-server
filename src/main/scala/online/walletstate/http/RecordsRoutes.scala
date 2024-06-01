package online.walletstate.http

import online.walletstate.http.endpoints.RecordsEndpoints
import online.walletstate.services.RecordsService
import zio.*
import zio.http.*

case class RecordsRoutes(recordsService: RecordsService) extends WalletStateRoutes with RecordsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(info => recordsService.create(info))
  }

  private val listRoute = list.implement {
    Handler.fromFunctionZIO((account, nextPageToken) => recordsService.list(account, nextPageToken))
  }

  private val getRoute = get.implement {
    Handler.fromFunctionZIO(id => recordsService.get(id))
  }

  private val updateRoute = update.implement {
    Handler.fromFunctionZIO((id, data) => recordsService.update(id, data))
  }

  private val deleteRoute = delete.implement {
    Handler.fromFunctionZIO(id => recordsService.delete(id))
  }

  override val walletRoutes = Routes(createRoute, listRoute, getRoute, updateRoute, deleteRoute)
}

object RecordsRoutes {
  val layer = ZLayer.fromFunction(RecordsRoutes.apply _)
}
