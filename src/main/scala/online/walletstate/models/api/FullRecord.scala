package online.walletstate.models.api

import online.walletstate.models.{Asset, Category, Record}
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class FullRecord(
    id: Record.Id,
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

object FullRecord {
  given schema: Schema[FullRecord] = DeriveSchema.gen
}
