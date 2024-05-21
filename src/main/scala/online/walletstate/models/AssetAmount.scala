package online.walletstate.models

import zio.schema.{DeriveSchema, Schema}

final case class AssetAmount(asset: Asset.Id, amount: BigDecimal)

object AssetAmount {
  given schema: Schema[AssetAmount] = DeriveSchema.gen
}
