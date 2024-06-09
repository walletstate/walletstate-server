package online.walletstate.http

import online.walletstate.http.endpoints.ExchangeRatesEndpoints
import online.walletstate.models.HttpError
import online.walletstate.services.ExchangeRatesService
import zio.*
import zio.http.*

final case class ExchangeRatesRoutes(exchangeRatesService: ExchangeRatesService)
    extends WalletStateRoutes
    with ExchangeRatesEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(info => exchangeRatesService.create(info))
  }

  private val listRoute = list.implement {
    Handler.fromFunctionZIO((from, to) => exchangeRatesService.list(from, to))
  }

  private val getRoute = get.implement {
    Handler.fromFunctionZIO(id => exchangeRatesService.get(id).mapError(HttpError.NotFound.apply))
  }

  override val walletRoutes = Routes(createRoute, listRoute, getRoute)
}

object ExchangeRatesRoutes {
  val layer = ZLayer.fromFunction(ExchangeRatesRoutes.apply _)
}
