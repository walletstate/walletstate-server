package online.walletstate.models.api

import online.walletstate.models.{Group, Icon}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class CreateCategory(
    group: Group.Id,
    name: String,
    icon: Option[Icon.Id],
    tags: List[String],
    idx: Int
)

object CreateCategory {
  given schema: Schema[CreateCategory]   = DeriveSchema.gen[CreateCategory]
}
