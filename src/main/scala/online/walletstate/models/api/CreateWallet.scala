package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class CreateWallet(name: String)

object CreateWallet {
  given codec: JsonCodec[CreateWallet] = DeriveJsonCodec.gen[CreateWallet]
  given schema: Schema[CreateWallet]   = DeriveSchema.gen[CreateWallet]
}
