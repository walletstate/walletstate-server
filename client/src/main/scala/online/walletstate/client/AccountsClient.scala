package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Account, AssetAmount, Grouped, HttpError, Page, Record}
import online.walletstate.http.endpoints.AccountsEndpoints
import zio.http.Header
import zio.http.Header.Authorization
import zio.http.endpoint.EndpointExecutor
import zio.{IO, ZIO, ZLayer}

final case class AccountsClient(executor: EndpointExecutor[Any, Authorization.Bearer]) extends AccountsEndpoints {

  def create(data: Account.Data): IO[BadRequest | Unauthorized | InternalServerError, Account] =
    ZIO.scoped(executor(createEndpoint(data)))

  def list: IO[Unauthorized | InternalServerError, List[Account]] =
    ZIO.scoped(executor(listEndpoint(())))

  def listGrouped: IO[Unauthorized | InternalServerError, List[Grouped[Account]]] =
    ZIO.scoped(executor(listGroupedEndpoint(())))

  def get(id: Account.Id): IO[Unauthorized | NotFound | InternalServerError, Account] =
    ZIO.scoped(executor(getEndpoint(id)))

  def update(
      id: Account.Id,
      data: Account.Data
  ): IO[BadRequest | Unauthorized | NotFound | InternalServerError, Unit] =
    ZIO.scoped(executor(updateEndpoint(id, data)))

  def listRecords(
      id: Account.Id,
      page: Option[Page.Token] = None
  ): IO[BadRequest | Unauthorized | InternalServerError, Page[Record.Full]] =
    ZIO.scoped(executor(listRecordsEndpoint(id, page)))

  def getBalance(id: Account.Id): IO[Unauthorized | InternalServerError, List[AssetAmount]] =
    ZIO.scoped(executor(getBalanceEndpoint(id)))
}

object AccountsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(AccountsClient.apply _)
}
