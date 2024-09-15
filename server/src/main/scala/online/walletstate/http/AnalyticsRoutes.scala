package online.walletstate.http

import online.walletstate.http.endpoints.AnalyticsEndpoints
import online.walletstate.services.AnalyticsService
import zio.ZLayer
import zio.http.{Handler, Routes}

final case class AnalyticsRoutes(analyticsService: AnalyticsService) extends WalletStateRoutes with AnalyticsEndpoints {

  private val recordsRoute   = recordsEndpoint.implement((filter, page) => analyticsService.records(filter, page))
  private val aggregateRoute = aggregatedEndpoint.implement(filter => analyticsService.aggregate(filter))
  private val groupRoute     = groupedEndpoint.implement(groupBy => analyticsService.group(groupBy))

  override val walletRoutes = Routes(recordsRoute, aggregateRoute, groupRoute)
}

object AnalyticsRoutes {
  val layer = ZLayer.fromFunction(AnalyticsRoutes.apply _)
}
