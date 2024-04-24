package online.walletstate.models.api

import zio.schema.{DeriveSchema, Schema}

final case class UpdateGroup(name: String, idx: Int)

object UpdateGroup {
  given schema: Schema[UpdateGroup]   = DeriveSchema.gen[UpdateGroup]
}
