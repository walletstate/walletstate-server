package online.walletstate.models.api

import online.walletstate.models.Asset

import java.time.ZonedDateTime
import zio.json.*

final case class CreateExchangeRate(from: Asset.Id, to: Asset.Id, rate: BigDecimal, datetime: ZonedDateTime)

object CreateExchangeRate {
  given codec: JsonCodec[CreateExchangeRate] = DeriveJsonCodec.gen[CreateExchangeRate]
}
