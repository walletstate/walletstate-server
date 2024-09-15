package online.walletstate.http

import online.walletstate.http.endpoints.ExchangeRatesEndpoints
import online.walletstate.services.ExchangeRatesService
import zio.*
import zio.http.*

final case class ExchangeRatesRoutes(exchangeRatesService: ExchangeRatesService)
    extends WalletStateRoutes
    with ExchangeRatesEndpoints {

  private val createRoute = createEndpoint.implement(info => exchangeRatesService.create(info))
  private val listRoute   = listEndpoint.implement((from, to) => exchangeRatesService.list(from, to))
  private val getRoute    = getEndpoint.implement(id => exchangeRatesService.get(id).mapError(_.asNotFound))

  override val walletRoutes = Routes(createRoute, listRoute, getRoute)
}

object ExchangeRatesRoutes {
  val layer = ZLayer.fromFunction(ExchangeRatesRoutes.apply _)
}
