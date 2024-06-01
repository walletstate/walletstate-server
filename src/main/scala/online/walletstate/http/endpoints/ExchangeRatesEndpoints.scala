package online.walletstate.http.endpoints

import online.walletstate.models.{AppError, Asset, ExchangeRate}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait ExchangeRatesEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "exchange-rates")
      .in[ExchangeRate.Data]
      .out[ExchangeRate](Status.Created)

  val list =
    Endpoint(Method.GET / "api" / "exchange-rates")
      .query[Asset.Id](Asset.Id.query("from"))
      .query[Asset.Id](Asset.Id.query("to"))
      .out[List[ExchangeRate]]

  val get =
    Endpoint(Method.GET / "api" / "exchange-rates" / ExchangeRate.Id.path)
      .out[ExchangeRate]
      .outError[AppError.ExchangeRateNotExist](Status.NotFound)

  override val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list
  )
}

object ExchangeRatesEndpoints extends ExchangeRatesEndpoints
