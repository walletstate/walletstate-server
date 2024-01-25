package online.walletstate.models.api

import online.walletstate.models.Asset
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.ZonedDateTime

final case class CreateAsset(
    `type`: Asset.Type,
    ticker: String,
    name: String,
    icon: String,
    tags: Seq[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    denominatedIn: Option[Asset.Id],
    denomination: Option[BigDecimal]
)

object CreateAsset {
  given codec: JsonCodec[CreateAsset] = DeriveJsonCodec.gen[CreateAsset]
}
