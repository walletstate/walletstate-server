package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, Unauthorized}
import online.walletstate.common.models.{Analytics, AssetAmount, Page, Record}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait AnalyticsEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "Analytics"

  val recordsEndpoint =
    Endpoint(Method.POST / "api" / "analytics" / "records").walletStateEndpoint
      .in[Analytics.Filter]
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.SingleTransaction]](Status.Ok)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val aggregatedEndpoint =
    Endpoint(Method.POST / "api" / "analytics" / "aggregated").walletStateEndpoint
      .in[Analytics.AggregateRequest]
      .out[List[AssetAmount]](Status.Ok)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val groupedEndpoint =
    Endpoint(Method.POST / "api" / "analytics" / "grouped").walletStateEndpoint
      .in[Analytics.GroupRequest]
      .out[List[Analytics.GroupedResult]](Status.Ok)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  override val endpointsMap = Map(
    "records"    -> recordsEndpoint,
    "aggregated" -> aggregatedEndpoint,
    "grouped"    -> groupedEndpoint
  )
}

object AnalyticsEndpoints extends AnalyticsEndpoints
