package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class JoinNamespace(inviteCode: String)

object JoinNamespace {
  given codec: JsonCodec[JoinNamespace] = DeriveJsonCodec.gen[JoinNamespace]
}
