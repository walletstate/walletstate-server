package online.walletstate.models.api

import zio.schema.{DeriveSchema, Schema}

final case class JoinWallet(inviteCode: String)

object JoinWallet {
  given schema: Schema[JoinWallet]   = DeriveSchema.gen[JoinWallet]
}
