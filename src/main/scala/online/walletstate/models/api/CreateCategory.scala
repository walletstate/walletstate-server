package online.walletstate.models.api

import online.walletstate.models.{Group, Icon}
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateCategory(
    group: Group.Id,
    name: String,
    icon: Option[Icon.Id],
    tags: Seq[String],
    orderingIndex: Int
)

object CreateCategory {
  given codec: JsonCodec[CreateCategory] = DeriveJsonCodec.gen[CreateCategory]
}
