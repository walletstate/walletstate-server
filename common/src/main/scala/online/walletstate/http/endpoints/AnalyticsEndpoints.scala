package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, Unauthorized}
import online.walletstate.common.models.{Analytics, AssetAmount, Page, Record}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Method, Status}

trait AnalyticsEndpoints extends WalletStateEndpoints {

  val recordsEndpoint =
    Endpoint(Method.POST / "api" / "analytics" / "records")
      .auth(AuthType.Bearer)
      .in[Analytics.Filter]
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.SingleTransaction]](Status.Ok)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val aggregatedEndpoint =
    Endpoint(Method.POST / "api" / "analytics" / "aggregated")
      .auth(AuthType.Bearer)
      .in[Analytics.AggregateRequest]
      .out[List[AssetAmount]](Status.Ok)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val groupedEndpoint =
    Endpoint(Method.POST / "api" / "analytics" / "grouped")
      .auth(AuthType.Bearer)
      .in[Analytics.GroupRequest]
      .out[List[Analytics.GroupedResult]](Status.Ok)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  override val endpointsMap = Map(
    "records"    -> recordsEndpoint,
    "aggregated" -> aggregatedEndpoint,
    "grouped"    -> groupedEndpoint
  )

  override val endpoints = Chunk(
    // records, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    aggregatedEndpoint,
    groupedEndpoint
  )

}

object AnalyticsEndpoints extends AnalyticsEndpoints
