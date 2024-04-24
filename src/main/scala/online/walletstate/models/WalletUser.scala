package online.walletstate.models

import zio.schema.{DeriveSchema, Schema}

final case class WalletUser(wallet: Wallet.Id, user: User.Id)

object WalletUser {
  given schema: Schema[WalletUser] = DeriveSchema.gen[WalletUser]
}
