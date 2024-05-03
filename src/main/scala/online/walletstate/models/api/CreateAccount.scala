package online.walletstate.models.api

import online.walletstate.models.{Asset, Group, Icon}
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class CreateAccount(
    group: Group.Id,
    name: String,
    defaultAsset: Option[Asset.Id],
    idx: Int,
    icon: Option[Icon.Id],
    tags: List[String]
)

object CreateAccount {
  given schema: Schema[CreateAccount] = DeriveSchema.gen[CreateAccount]
}
