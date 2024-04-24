package online.walletstate.models

import zio.schema.{DeriveSchema, Schema}

final case class AssetBalance(asset: Asset.Id, amount: BigDecimal)

object AssetBalance {
  given schema: Schema[AssetBalance] = DeriveSchema.gen
}
