package online.walletstate.models.api

import online.walletstate.models.{Group, Icon}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class CreateAccount(
    group: Group.Id,
    name: String,
    orderingIndex: Int,
    icon: Option[Icon.Id],
    tags: Chunk[String]
)

object CreateAccount {
  given schema: Schema[CreateAccount] = DeriveSchema.gen[CreateAccount]
}
