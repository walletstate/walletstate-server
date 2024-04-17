package online.walletstate.models.api

import online.walletstate.models.Asset

import java.time.ZonedDateTime
import zio.json.*
import zio.schema.{DeriveSchema, Schema}

final case class CreateExchangeRate(from: Asset.Id, to: Asset.Id, rate: BigDecimal, datetime: ZonedDateTime)

object CreateExchangeRate {
  given codec: JsonCodec[CreateExchangeRate] = DeriveJsonCodec.gen[CreateExchangeRate]
  given schema: Schema[CreateExchangeRate] = DeriveSchema.gen[CreateExchangeRate]
}
