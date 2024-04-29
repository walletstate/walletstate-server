package online.walletstate.models.api

import online.walletstate.models.{Asset, Icon}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class UpdateAsset(
    `type`: Asset.Type,
    ticker: String,
    name: String,
    icon: Option[Icon.Id],
    tags: List[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    denominatedIn: Option[Asset.Id],
    denomination: Option[BigDecimal]
)

object UpdateAsset {
  given schema: Schema[UpdateAsset] = DeriveSchema.gen[UpdateAsset]
}
