//package online.walletstate.config
//
//import zio.*
//import zio.config.*
//import zio.config.magnolia.*
//
//case class DbConfig(host: String, port: Int, name: String, user: String, password: String)
//
//object DbConfig {
//  val config: Config[DbConfig] = deriveConfig[DbConfig].nested("db").mapKey(toKebabCase)
//}
