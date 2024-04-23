package online.walletstate.models.api

import online.walletstate.models.{Asset, Icon}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class CreateAsset(
    `type`: Asset.Type,
    ticker: String,
    name: String,
    icon: Option[Icon.Id],
    tags: Chunk[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    denominatedIn: Option[Asset.Id],
    denomination: Option[BigDecimal]
)

object CreateAsset {
  given schema: Schema[CreateAsset] = DeriveSchema.gen[CreateAsset]
}
