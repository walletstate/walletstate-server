package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class LoginInfo(username: String, password: String)

object LoginInfo {
  given codec: JsonCodec[LoginInfo] = DeriveJsonCodec.gen[LoginInfo]
}
