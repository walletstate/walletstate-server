package online.walletstate.http.api.endpoints

import online.walletstate.models.{Asset, ExchangeRate}
import online.walletstate.models.api.CreateExchangeRate
import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait ExchangeRatesEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "exchange-rates")
      .in[CreateExchangeRate]
      .out[ExchangeRate](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "exchange-rates")
      .query[Asset.Id](Asset.Id.query("from"))
      .query[Asset.Id](Asset.Id.query("to"))
      .out[Chunk[ExchangeRate]]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "exchange-rates" / ExchangeRate.Id.path)
      .out[ExchangeRate]
      .outError[UnauthorizedError.type](Status.Unauthorized)
  
  val endpointsMap = Map(
    "create" -> create,
    "get" -> get,
    "list" -> list
  )

  val endpoints = endpointsMap.values

}

object ExchangeRatesEndpoints extends ExchangeRatesEndpoints
