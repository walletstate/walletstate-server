package online.walletstate.config

import zio.{Config, ZIO, ZLayer}
import zio.config.*
import zio.config.magnolia.*
import zio.http.*

case class HttpServerConfig(port: Int)

object HttpServerConfig {
  private val config: Config[HttpServerConfig] = deriveConfig[HttpServerConfig].nested("server")

  val serverConfigLayer: ZLayer[Any, Config.Error, Server.Config] =
    ZLayer.fromZIO(
      ZIO.config[HttpServerConfig](config).map { c =>
        Server.Config.default.port(c.port)
      }
    )
}
