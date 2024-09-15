package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Asset, ExchangeRate}
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait ExchangeRatesEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "ExchangeRates"

  val createEndpoint =
    Endpoint(Method.POST / "api" / "exchange-rates").walletStateEndpoint
      .in[ExchangeRate.Data]
      .out[ExchangeRate](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listEndpoint =
    Endpoint(Method.GET / "api" / "exchange-rates").walletStateEndpoint
      .query[Asset.Id](Asset.Id.query("from"))
      .query[Asset.Id](Asset.Id.query("to"))
      .out[List[ExchangeRate]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val getEndpoint =
    Endpoint(Method.GET / "api" / "exchange-rates" / ExchangeRate.Id.path).walletStateEndpoint
      .out[ExchangeRate]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  override val endpointsMap = Map(
    "create" -> createEndpoint,
    "get"    -> getEndpoint,
    "list"   -> listEndpoint
  )
}

object ExchangeRatesEndpoints extends ExchangeRatesEndpoints
