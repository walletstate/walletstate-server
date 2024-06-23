package online.walletstate.http

import online.walletstate.http.endpoints.ExchangeRatesEndpoints
import online.walletstate.services.ExchangeRatesService
import zio.*
import zio.http.*

final case class ExchangeRatesRoutes(exchangeRatesService: ExchangeRatesService)
    extends WalletStateRoutes
    with ExchangeRatesEndpoints {

  private val createRoute = createEndpoint.implement {
    Handler.fromFunctionZIO(info => exchangeRatesService.create(info))
  }

  private val listRoute = listEndpoint.implement {
    Handler.fromFunctionZIO((from, to) => exchangeRatesService.list(from, to))
  }

  private val getRoute = getEndpoint.implement {
    Handler.fromFunctionZIO(id => exchangeRatesService.get(id).mapError(_.asNotFound))
  }

  override val walletRoutes = Routes(createRoute, listRoute, getRoute)
}

object ExchangeRatesRoutes {
  val layer = ZLayer.fromFunction(ExchangeRatesRoutes.apply _)
}
