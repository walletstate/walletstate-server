package online.walletstate.models.api

import online.walletstate.models.{Group, Icon}
import zio.schema.{DeriveSchema, Schema}

final case class UpdateAccount(group: Group.Id, name: String, idx: Int, icon: Option[Icon.Id], tags: List[String])

object UpdateAccount {
  given schema: Schema[UpdateAccount] = DeriveSchema.gen
}
