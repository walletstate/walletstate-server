package online.walletstate.models.api

import online.walletstate.models.{Asset, Category, Record}
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

case class SingleTransactionRecord(
    id: Record.Id,
    `type`: Record.Type,
    transaction: TransactionData,
    category: Category.Id,
    datetime: ZonedDateTime,
    description: Option[String],
    tags: List[String],
    externalId: Option[String],
    spentOn: Option[Asset.Id],
    generatedBy: Option[Asset.Id]
)

object SingleTransactionRecord {
  given schema: Schema[SingleTransactionRecord] = DeriveSchema.gen
}
