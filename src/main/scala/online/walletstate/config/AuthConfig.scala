package online.walletstate.config

import zio.*
import zio.config.*
import zio.config.magnolia.*

case class AuthConfig(secret: String, tokenTTL: Duration, identityProvider: IdPConfig)

sealed trait IdPConfig

object IdPConfig {
  case class ConfiguredUsers(users: List[TestUserIdentity]) extends IdPConfig
  case class SSO(callbackUrl: String, flow: String)         extends IdPConfig // TODO implement later

  case class TestUserIdentity(id: String, username: String, password: String)
}

object AuthConfig {
  val config: Config[AuthConfig] = deriveConfig[AuthConfig].nested("auth").mapKey(toKebabCase)

  val layer: ZLayer[ConfigProvider, Config.Error, AuthConfig] = ZLayer.fromZIO(ZIO.config(config))
}
