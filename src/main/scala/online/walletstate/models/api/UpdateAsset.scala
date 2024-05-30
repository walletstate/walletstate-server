package online.walletstate.models.api

import online.walletstate.models.{Asset, Group, Icon}
import zio.{Chunk, Duration}
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class UpdateAsset(
    group: Group.Id,
    `type`: Asset.Type,
    ticker: String,
    name: String,
    icon: Option[Icon.Id],
    tags: List[String],
    idx: Int,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    lockDuration: Option[Duration],
    unlockDuration: Option[Duration],
    denominatedIn: Option[Asset.Id],
    denomination: Option[BigDecimal]
)

object UpdateAsset {
  given schema: Schema[UpdateAsset] = DeriveSchema.gen[UpdateAsset]
}
