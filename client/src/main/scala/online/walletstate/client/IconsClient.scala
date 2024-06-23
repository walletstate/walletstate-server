package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, Unauthorized}
import online.walletstate.common.models.{HttpError, Icon}
import online.walletstate.http.endpoints.IconsEndpoints
import zio.{IO, ZIO, ZLayer}
import zio.http.Header
import zio.http.endpoint.EndpointExecutor

final case class IconsClient(executor: EndpointExecutor[Header.Authorization]) extends IconsEndpoints {

  def list(tag: Option[String] = None): IO[BadRequest | Unauthorized | InternalServerError, List[Icon.Id]] =
    ZIO.scoped(executor(listEndpoint(tag)))

  def create(data: Icon.Data): IO[BadRequest | Unauthorized | InternalServerError, Icon.Id] =
    ZIO.scoped(executor(createEndpoint(data)))
}

object IconsClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(IconsClient.apply _)
}
