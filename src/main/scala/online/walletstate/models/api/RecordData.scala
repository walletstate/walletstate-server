package online.walletstate.models.api

import online.walletstate.models.{Asset, Category, Record}
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

case class RecordData(
    `type`: Record.Type,
    from: Option[TransactionData],
    to: Option[TransactionData],
    category: Category.Id,
    datetime: ZonedDateTime,
    description: Option[String],
    tags: List[String],
    externalId: Option[String],
    spentOn: Option[Asset.Id],
    generatedBy: Option[Asset.Id]
)

object RecordData {
  given schema: Schema[RecordData] = DeriveSchema.gen
}
