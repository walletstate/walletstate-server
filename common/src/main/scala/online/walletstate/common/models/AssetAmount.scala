package online.walletstate.common.models

import zio.schema.{DeriveSchema, Schema}

final case class AssetAmount(asset: Asset.Id, amount: BigDecimal)

object AssetAmount {
  given schema: Schema[AssetAmount] = DeriveSchema.gen
}
