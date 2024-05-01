package online.walletstate.models.api

import online.walletstate.models.{Asset, Group, Icon}
import zio.schema.{DeriveSchema, Schema}

final case class UpdateAccount(
    group: Group.Id,
    name: String,
    defaultAsset: Option[Asset.Id],
    idx: Int,
    icon: Option[Icon.Id],
    tags: List[String]
)

object UpdateAccount {
  given schema: Schema[UpdateAccount] = DeriveSchema.gen
}
