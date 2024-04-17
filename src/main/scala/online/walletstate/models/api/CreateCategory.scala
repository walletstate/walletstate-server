package online.walletstate.models.api

import online.walletstate.models.{Group, Icon}
import zio.Chunk
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class CreateCategory(
    group: Group.Id,
    name: String,
    icon: Option[Icon.Id],
    tags: Chunk[String],
    orderingIndex: Int
)

object CreateCategory {
  given codec: JsonCodec[CreateCategory] = DeriveJsonCodec.gen[CreateCategory]
  given schema: Schema[CreateCategory]   = DeriveSchema.gen[CreateCategory]
}
