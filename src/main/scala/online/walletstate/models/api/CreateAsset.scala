package online.walletstate.models.api

import online.walletstate.models.{Asset, Group, Icon}
import zio.{Chunk, Duration}
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class CreateAsset(
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

object CreateAsset {
  given schema: Schema[CreateAsset] = DeriveSchema.gen[CreateAsset]
}
