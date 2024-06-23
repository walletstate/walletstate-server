package online.walletstate.http

import online.walletstate.http.endpoints.AnalyticsEndpoints
import online.walletstate.services.AnalyticsService
import zio.ZLayer
import zio.http.{Handler, Routes}

final case class AnalyticsRoutes(analyticsService: AnalyticsService) extends WalletStateRoutes with AnalyticsEndpoints {

  val recordsRoute = recordsEndpoint.implement {
    Handler.fromFunctionZIO((filter, page) => analyticsService.records(filter, page))
  }

  val aggregateRoute = aggregatedEndpoint.implement {
    Handler.fromFunctionZIO(filter => analyticsService.aggregate(filter))
  }

  val groupRoute = groupedEndpoint.implement {
    Handler.fromFunctionZIO(groupBy => analyticsService.group(groupBy))
  }

  override val walletRoutes = Routes(recordsRoute, aggregateRoute, groupRoute)
}

object AnalyticsRoutes {
  val layer = ZLayer.fromFunction(AnalyticsRoutes.apply _)
}
