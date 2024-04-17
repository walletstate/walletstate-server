package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class JoinWallet(inviteCode: String)

object JoinWallet {
  given codec: JsonCodec[JoinWallet] = DeriveJsonCodec.gen[JoinWallet]
  given schema: Schema[JoinWallet]   = DeriveSchema.gen[JoinWallet]
}
