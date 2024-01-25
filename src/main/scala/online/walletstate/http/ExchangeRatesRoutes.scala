package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.{Asset, ExchangeRate}
import online.walletstate.models.api.CreateExchangeRate
import online.walletstate.services.ExchangeRatesService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

final case class ExchangeRatesRoutes(auth: AuthMiddleware, exchangeRatesService: ExchangeRatesService) {

  private val createHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      info <- req.as[CreateExchangeRate]
      rate <- exchangeRatesService.create(ctx.wallet, info)
    } yield Response.json(rate.toJson)
  }

  private val getExchangeRatesHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      from  <- ZIO.fromOption(req.url.queryParams.get("from")).flatMap(Asset.Id.from) // TODO add correct errors
      to    <- ZIO.fromOption(req.url.queryParams.get("to")).flatMap(Asset.Id.from)   // TODO add correct errors
      rates <- exchangeRatesService.list(ctx.wallet, from, to)
    } yield Response.json(rates.toJson)
  }

  private val getExchangeRateHandler = Handler.fromFunctionZIO[(ExchangeRate.Id, WalletContext, Request)] {
    (id, ctx, req) =>
      for {
        rate <- exchangeRatesService.get(ctx.wallet, id)
      } yield Response.json(rate.toJson)
  }

  val routes = Routes(
    Method.POST / "api" / "exchange-rates"                       -> auth.walletCtx -> createHandler,
    Method.GET / "api" / "exchange-rates"                        -> auth.walletCtx -> getExchangeRatesHandler,
    Method.GET / "api" / "exchange-rates" / ExchangeRate.Id.path -> auth.walletCtx -> getExchangeRateHandler
  )

}

object ExchangeRatesRoutes {
  val layer = ZLayer.fromFunction(ExchangeRatesRoutes.apply _)
}
