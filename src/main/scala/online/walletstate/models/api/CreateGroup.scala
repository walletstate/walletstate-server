package online.walletstate.models.api

import online.walletstate.models.Group
import zio.schema.{DeriveSchema, Schema}

final case class CreateGroup(name: String, `type`: Group.Type, idx: Int)

object CreateGroup {
  given schema: Schema[CreateGroup] = DeriveSchema.gen[CreateGroup]
}
