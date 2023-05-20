package online.walletstate.domain.auth

import zio.json.*

case class LoginInfo(username: String, password: String)

object LoginInfo {
  given encoder: JsonEncoder[LoginInfo] = DeriveJsonEncoder.gen[LoginInfo]
  given decoder: JsonDecoder[LoginInfo] = DeriveJsonDecoder.gen[LoginInfo]
}
