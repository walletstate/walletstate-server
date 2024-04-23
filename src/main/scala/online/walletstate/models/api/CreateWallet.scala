package online.walletstate.models.api

import zio.schema.{DeriveSchema, Schema}

final case class CreateWallet(name: String)

object CreateWallet {
  given schema: Schema[CreateWallet]   = DeriveSchema.gen[CreateWallet]
}
