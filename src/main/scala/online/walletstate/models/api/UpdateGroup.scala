package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class UpdateGroup(name: String, orderingIndex: Int)

object UpdateGroup {
  given codec: JsonCodec[UpdateGroup] = DeriveJsonCodec.gen[UpdateGroup]
  given schema: Schema[UpdateGroup]   = DeriveSchema.gen[UpdateGroup]
}
