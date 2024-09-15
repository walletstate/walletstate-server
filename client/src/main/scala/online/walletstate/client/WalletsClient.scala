package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{HttpError, Wallet, WalletInvite}
import online.walletstate.http.endpoints.WalletStateEndpoints.Auth.ClientAuthRequirement
import online.walletstate.http.endpoints.WalletsEndpoints
import zio.http.endpoint.EndpointExecutor
import zio.{IO, ZIO, ZLayer}

final case class WalletsClient(executor: EndpointExecutor[Any, ClientAuthRequirement]) extends WalletsEndpoints {

  def get: IO[Unauthorized | NotFound | InternalServerError, Wallet] =
    ZIO.scoped(executor(getCurrentEndpoint(())))

  def createInvite: IO[Unauthorized | NotFound | InternalServerError, WalletInvite] =
    ZIO.scoped(executor(createInviteEndpoint(())))
}

object WalletsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(WalletsClient.apply _)
}
