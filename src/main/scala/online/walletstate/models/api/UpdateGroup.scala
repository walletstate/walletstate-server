package online.walletstate.models.api

import zio.schema.{DeriveSchema, Schema}

final case class UpdateGroup(name: String, orderingIndex: Int)

object UpdateGroup {
  given schema: Schema[UpdateGroup]   = DeriveSchema.gen[UpdateGroup]
}
