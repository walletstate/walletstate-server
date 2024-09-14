package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, Unauthorized}
import online.walletstate.common.models.{Analytics, AssetAmount, HttpError, Page, Record}
import online.walletstate.http.endpoints.AnalyticsEndpoints
import zio.{IO, ZIO, ZLayer}
import zio.http.Header
import zio.http.Header.Authorization
import zio.http.endpoint.EndpointExecutor

final case class AnalyticsClient(executor: EndpointExecutor[Any, Authorization.Bearer]) extends AnalyticsEndpoints {

  def records(
      filter: Analytics.Filter,
      page: Option[Page.Token] = None
  ): IO[BadRequest | Unauthorized | InternalServerError, Page[Record.SingleTransaction]] =
    ZIO.scoped(executor(recordsEndpoint(filter, page)))

  def aggregated(
      request: Analytics.AggregateRequest
  ): ZIO[Any, BadRequest | Unauthorized | InternalServerError, List[AssetAmount]] =
    ZIO.scoped(executor(aggregatedEndpoint(request)))

  def grouped(
      request: Analytics.GroupRequest
  ): ZIO[Any, BadRequest | Unauthorized | InternalServerError, List[Analytics.GroupedResult]] =
    ZIO.scoped(executor(groupedEndpoint(request)))
}

object AnalyticsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(AnalyticsClient.apply _)
}
