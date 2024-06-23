package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Category, Grouped, HttpError}
import online.walletstate.http.endpoints.CategoriesEndpoints
import zio.{IO, ZIO, ZLayer}
import zio.http.Header
import zio.http.endpoint.EndpointExecutor

final case class CategoriesClient(executor: EndpointExecutor[Header.Authorization]) extends CategoriesEndpoints {

  def create(data: Category.Data): IO[BadRequest | Unauthorized | InternalServerError, Category] =
    ZIO.scoped(executor(createEndpoint(data)))

  def list: IO[Unauthorized | InternalServerError, List[Category]] = ZIO.scoped(executor(listEndpoint(())))

  def listGrouped: IO[Unauthorized | InternalServerError, List[Grouped[Category]]] =
    ZIO.scoped(executor(listGroupedEndpoint(())))

  def get(id: Category.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Category] =
    ZIO.scoped(executor(getEndpoint(id)))

  def update(
      id: Category.Id,
      data: Category.Data
  ): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Unit] =
    ZIO.scoped(executor(updateEndpoint(id, data)))
}

object CategoriesClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(CategoriesClient.apply _)
}
