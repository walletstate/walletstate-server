package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Asset, Grouped}
import online.walletstate.http.endpoints.AssetsEndpoints
import online.walletstate.http.endpoints.WalletStateEndpoints.Auth.ClientAuthRequirement
import zio.http.endpoint.EndpointExecutor
import zio.{IO, ZIO, ZLayer}

final case class AssetsClient(executor: EndpointExecutor[Any, ClientAuthRequirement]) extends AssetsEndpoints {
  def create(data: Asset.Data): IO[BadRequest | Unauthorized | InternalServerError, Asset] =
    ZIO.scoped(executor(createEndpoint(data)))

  def list: IO[Unauthorized | InternalServerError, List[Asset]] = ZIO.scoped(executor(listEndpoint(())))

  def listGrouped: IO[Unauthorized | InternalServerError, List[Grouped[Asset]]] =
    ZIO.scoped(executor(listGroupedEndpoint(())))

  def get(id: Asset.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Asset] =
    ZIO.scoped(executor(getEndpoint(id)))

  def update(
      id: Asset.Id,
      data: Asset.Data
  ): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Unit] =
    ZIO.scoped(executor(updateEndpoint(id, data)))
}

object AssetsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(AssetsClient.apply _)
}
