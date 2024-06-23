package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Group, HttpError}
import online.walletstate.http.endpoints.GroupsEndpoints
import zio.{IO, ZIO, ZLayer}
import zio.http.Header
import zio.http.endpoint.EndpointExecutor

final case class GroupsClient(executor: EndpointExecutor[Header.Authorization]) extends GroupsEndpoints {

  def create(data: Group.CreateData): IO[BadRequest | Unauthorized | InternalServerError, Group] =
    ZIO.scoped(executor(createEndpoint(data)))

  def list(groupType: Group.Type): IO[BadRequest | Unauthorized | InternalServerError, List[Group]] =
    ZIO.scoped(executor(listEndpoint(groupType)))

  def get(id: Group.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Group] =
    ZIO.scoped(executor(getEndpoint(id)))

  def update(
      id: Group.Id,
      data: Group.UpdateData
  ): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Unit] =
    ZIO.scoped(executor(updateEndpoint(id, data)))

  def delete(id: Group.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Unit] =
    ZIO.scoped(executor(deleteEndpoint(id)))
}

object GroupsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(GroupsClient.apply _)
}
