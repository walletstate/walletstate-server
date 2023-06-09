package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class JoinWallet(inviteCode: String)

object JoinWallet {
  given codec: JsonCodec[JoinWallet] = DeriveJsonCodec.gen[JoinWallet]
}
