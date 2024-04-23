package online.walletstate.models.api

import zio.schema.{DeriveSchema, Schema}

final case class CreateGroup(name: String, orderingIndex: Int)

object CreateGroup {
  given schema: Schema[CreateGroup] = DeriveSchema.gen[CreateGroup]
}
