package online.walletstate.http

import online.walletstate.http.api.AnalyticsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.{Analytics, Page}
import online.walletstate.services.AnalyticsService
import zio.ZLayer
import zio.http.{Handler, Routes}

final case class AnalyticsRoutes(auth: AuthMiddleware, analyticsService: AnalyticsService) extends AnalyticsEndpoints {
  import auth.implementWithWalletCtx

  val recordsRoute = records.implementWithWalletCtx[(Analytics.Filter, Option[Page.Token], WalletContext)] {
    Handler.fromFunctionZIO((filter, page, ctx) => analyticsService.records(ctx.wallet, filter, page))
  }()

  val routes = Routes(recordsRoute)
}

object AnalyticsRoutes {
  val layer = ZLayer.fromFunction(AnalyticsRoutes.apply _)
}
