package online.walletstate.http.api

import online.walletstate.models.api.SingleTransactionRecord
import online.walletstate.models.{Analytics, AppError, Page}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait AnalyticsEndpoints {

  val records =
    Endpoint(Method.POST / "api" / "analytics" / "records")
      .in[Analytics.Filter]
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[SingleTransactionRecord]](Status.Ok)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val endpointsMap = Map(
    "records" -> records
  )

  val endpoints = Chunk(
    // records, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
  )

}

object AnalyticsEndpoints extends AnalyticsEndpoints
