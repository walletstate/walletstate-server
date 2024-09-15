package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Asset, ExchangeRate, HttpError}
import online.walletstate.http.endpoints.ExchangeRatesEndpoints
import online.walletstate.http.endpoints.WalletStateEndpoints.Auth.ClientAuthRequirement
import zio.http.endpoint.EndpointExecutor
import zio.{IO, ZIO, ZLayer}

final case class ExchangeRatesClient(executor: EndpointExecutor[Any, ClientAuthRequirement])
    extends ExchangeRatesEndpoints {

  def create(data: ExchangeRate.Data): IO[BadRequest | Unauthorized | InternalServerError, ExchangeRate] =
    ZIO.scoped(executor(createEndpoint(data)))

  def list(from: Asset.Id, to: Asset.Id): IO[BadRequest | Unauthorized | InternalServerError, List[ExchangeRate]] =
    ZIO.scoped(executor(listEndpoint(from, to)))

  def get(id: ExchangeRate.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, ExchangeRate] =
    ZIO.scoped(executor(getEndpoint(id)))
}

object ExchangeRatesClient {
  val layer =
    WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(ExchangeRatesClient.apply _)
}
