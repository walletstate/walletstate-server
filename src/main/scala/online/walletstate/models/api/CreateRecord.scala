package online.walletstate.models.api

import online.walletstate.models.{Account, Category, RecordType}
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.Instant

final case class CreateRecord(
    account: Account.Id,
    amount: BigDecimal,
    `type`: RecordType,
    category: Category.Id,
    description: Option[String],
    time: Instant
)

object CreateRecord {
  given codec: JsonCodec[CreateRecord] = DeriveJsonCodec.gen[CreateRecord]
}
