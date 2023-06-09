package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateWallet(name: String)

object CreateWallet {
  given codec: JsonCodec[CreateWallet] = DeriveJsonCodec.gen[CreateWallet]
}
