package online.walletstate.client.configs

import zio.{Config, ZIO, ZLayer}
import zio.config.magnolia.deriveConfig
import zio.http.{Client, Header, URL}
import zio.http.endpoint.{EndpointExecutor, EndpointLocator}

case class WalletStateClientConfig(server: String, token: Config.Secret)

object WalletStateClientConfig {
  val configuredEndpointExecutorLayer = ZLayer {
    for {
      client    <- ZIO.service[Client]
      config    <- ZIO.config(deriveConfig[WalletStateClientConfig].nested("client").nested("walletstate"))
      serverUrl <- ZIO.fromEither(URL.decode(config.server)).orDie
    } yield EndpointExecutor(
      client,
      EndpointLocator.fromURL(serverUrl),
      ZIO.succeed(Header.Authorization.Bearer(config.token))
    )
  }
}
