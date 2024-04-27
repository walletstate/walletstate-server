package online.walletstate.http.api

import online.walletstate.models.{AppError, Asset, ExchangeRate}
import online.walletstate.models.api.CreateExchangeRate
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait ExchangeRatesEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "exchange-rates")
      .in[CreateExchangeRate]
      .out[ExchangeRate](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "exchange-rates")
      .query[Asset.Id](Asset.Id.query("from"))
      .query[Asset.Id](Asset.Id.query("to"))
      .out[List[ExchangeRate]]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "exchange-rates" / ExchangeRate.Id.path)
      .out[ExchangeRate]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list
  )

  val endpoints = endpointsMap.values

}

object ExchangeRatesEndpoints extends ExchangeRatesEndpoints
