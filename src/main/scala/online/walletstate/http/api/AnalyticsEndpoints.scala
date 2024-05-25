package online.walletstate.http.api

import online.walletstate.models.api.SingleTransactionRecord
import online.walletstate.models.{Analytics, AppError, AssetAmount, Page}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait AnalyticsEndpoints {

  val records =
    Endpoint(Method.POST / "api" / "analytics" / "records")
      .in[Analytics.Filter]
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[SingleTransactionRecord]](Status.Ok)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val aggregated =
      Endpoint(Method.POST / "api" / "analytics" / "aggregated")
        .in[Analytics.Filter]
        .out[List[AssetAmount]](Status.Ok)
        .outError[AppError.Unauthorized](Status.Unauthorized)
        .outError[AppError.BadRequest](Status.BadRequest)

  val grouped =
    Endpoint(Method.POST / "api" / "analytics" / "grouped")
      .in[Analytics.GroupRequest]
      .out[List[Analytics.GroupedResult]](Status.Ok)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)


  val endpointsMap = Map(
    "records" -> records,
    "aggregated" -> aggregated,
    "grouped" -> grouped
  )

  val endpoints = Chunk(
    // records, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    aggregated,
    grouped
  )

}

object AnalyticsEndpoints extends AnalyticsEndpoints
