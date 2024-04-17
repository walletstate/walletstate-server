package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class CreateGroup(name: String, orderingIndex: Int)

object CreateGroup {
  given codec: JsonCodec[CreateGroup] = DeriveJsonCodec.gen[CreateGroup]
  given schema: Schema[CreateGroup] = DeriveSchema.gen[CreateGroup]
}
