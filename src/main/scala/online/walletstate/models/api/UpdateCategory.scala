package online.walletstate.models.api

import online.walletstate.models.{Group, Icon}
import zio.schema.{DeriveSchema, Schema}

final case class UpdateCategory(group: Group.Id, name: String, icon: Option[Icon.Id], tags: List[String], idx: Int)

object UpdateCategory {
  given schema: Schema[UpdateCategory] = DeriveSchema.gen
}
