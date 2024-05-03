package online.walletstate.models.api

import online.walletstate.models.{Account, Asset}
import zio.schema.{DeriveSchema, Schema}

final case class TransactionData(account: Account.Id, asset: Asset.Id, amount: BigDecimal) 

object TransactionData {
  given schema: Schema[TransactionData] = DeriveSchema.gen
}
