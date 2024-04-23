package online.walletstate.http

import online.walletstate.http.api.endpoints.ExchangeRatesEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.{Asset, ExchangeRate}
import online.walletstate.models.api.CreateExchangeRate
import online.walletstate.services.ExchangeRatesService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

final case class ExchangeRatesRoutes(auth: AuthMiddleware, exchangeRatesService: ExchangeRatesService)
    extends ExchangeRatesEndpoints {
  import auth.implementWithWalletCtx
  
  private val createRoute = create.implementWithWalletCtx[(CreateExchangeRate, WalletContext)] {
    Handler.fromFunctionZIO((info, ctx) => exchangeRatesService.create(ctx.wallet, info))
  }()

  private val listRoute = list.implementWithWalletCtx[(Asset.Id, Asset.Id, WalletContext)]{
    Handler.fromFunctionZIO((from, to, ctx) => exchangeRatesService.list(ctx.wallet, from, to).map(Chunk.from))
  }()
  
  private val getRoute = get.implementWithWalletCtx[(ExchangeRate.Id, WalletContext)]{
    Handler.fromFunctionZIO((id, ctx) => exchangeRatesService.get(ctx.wallet, id))
  }()
  

  val routes = Routes(createRoute, listRoute, getRoute)
}

object ExchangeRatesRoutes {
  val layer = ZLayer.fromFunction(ExchangeRatesRoutes.apply _)
}
