package online.walletstate.http.endpoints

import online.walletstate.common.models.{Analytics, AssetAmount, Page, Record}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait AnalyticsEndpoints extends WalletStateEndpoints {

  val records =
    Endpoint(Method.POST / "api" / "analytics" / "records")
      .in[Analytics.Filter]
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.SingleTransaction]](Status.Ok)
      .withBadRequestCodec

  val aggregated =
      Endpoint(Method.POST / "api" / "analytics" / "aggregated")
        .in[Analytics.AggregateRequest]
        .out[List[AssetAmount]](Status.Ok)
        .withBadRequestCodec


  val grouped =
    Endpoint(Method.POST / "api" / "analytics" / "grouped")
      .in[Analytics.GroupRequest]
      .out[List[Analytics.GroupedResult]](Status.Ok)
      .withBadRequestCodec


  override val endpointsMap = Map(
    "records" -> records,
    "aggregated" -> aggregated,
    "grouped" -> grouped
  )

  override val endpoints = Chunk(
    // records, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    aggregated,
    grouped
  )

}

object AnalyticsEndpoints extends AnalyticsEndpoints
