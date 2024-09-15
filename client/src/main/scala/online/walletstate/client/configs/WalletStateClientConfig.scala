package online.walletstate.client.configs

import online.walletstate.http.endpoints.WalletStateEndpoints.Auth
import zio.config.magnolia.deriveConfig
import zio.http.endpoint.{EndpointExecutor, EndpointLocator}
import zio.http.{Client, URL}
import zio.{Config, ZIO, ZLayer}

case class WalletStateClientConfig(server: String, token: Config.Secret)

object WalletStateClientConfig {
  val configuredEndpointExecutorLayer: ZLayer[Client, Config.Error, EndpointExecutor[Any, Auth.ClientAuthRequirement]] =
    ZLayer {
      for {
        client    <- ZIO.service[Client]
        config    <- ZIO.config(deriveConfig[WalletStateClientConfig].nested("client").nested("walletstate"))
        serverUrl <- ZIO.fromEither(URL.decode(config.server)).orDie
      } yield EndpointExecutor(
        client,
        EndpointLocator.fromURL(serverUrl),
        Auth.bearer(config.token)
      )
    }
}
