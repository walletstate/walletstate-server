package online.walletstate.models.api

import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class CreateIcon(contentType: String, content: String, tags: List[String] = List.empty)

object CreateIcon {
  given schema: Schema[CreateIcon] = DeriveSchema.gen
}
