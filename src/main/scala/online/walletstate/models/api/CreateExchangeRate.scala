package online.walletstate.models.api

import online.walletstate.models.Asset
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class CreateExchangeRate(from: Asset.Id, to: Asset.Id, rate: BigDecimal, datetime: ZonedDateTime)

object CreateExchangeRate {
  given schema: Schema[CreateExchangeRate] = DeriveSchema.gen[CreateExchangeRate]
}
