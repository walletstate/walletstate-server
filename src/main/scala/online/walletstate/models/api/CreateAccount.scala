package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateAccount(name: String)

object CreateAccount {
  given codec: JsonCodec[CreateAccount] = DeriveJsonCodec.gen[CreateAccount]
}
