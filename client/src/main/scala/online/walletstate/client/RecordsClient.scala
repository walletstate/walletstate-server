package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Account, HttpError, Page, Record}
import online.walletstate.http.endpoints.RecordsEndpoints
import zio.http.Header
import zio.http.Header.Authorization
import zio.http.endpoint.EndpointExecutor
import zio.{IO, ZIO, ZLayer}

final case class RecordsClient(executor: EndpointExecutor[Any, Authorization.Bearer]) extends RecordsEndpoints {

  def create(data: Record.Data): IO[BadRequest | Unauthorized | InternalServerError, Record.Full] =
    ZIO.scoped(executor(createEndpoint(data)))

  def list(
      accountId: Account.Id,
      page: Option[Page.Token] = None
  ): IO[BadRequest | Unauthorized | InternalServerError, Page[Record.Full]] =
    ZIO.scoped(executor(listEndpoint(accountId, page)))

  def get(id: Record.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Record.Full] =
    ZIO.scoped(executor(getEndpoint(id)))

  def update(
      id: Record.Id,
      data: Record.Data
  ): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Record.Full] =
    ZIO.scoped(executor(updateEndpoint(id, data)))

  def delete(id: Record.Id): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Unit] =
    ZIO.scoped(executor(deleteEndpoint(id)))
}

object RecordsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(RecordsClient.apply _)
}
