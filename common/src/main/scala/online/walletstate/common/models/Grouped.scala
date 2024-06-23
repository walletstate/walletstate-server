package online.walletstate.common.models

import zio.Chunk
import online.walletstate.annotations.genericField
import zio.schema.{DeriveSchema, Schema}

final case class Grouped[T <: Groupable](
    id: Group.Id,
    name: String,
    idx: Int,
    @genericField() items: List[T]
)

object Grouped {

  given schema[T <: Groupable: Schema]: Schema[Grouped[T]] = DeriveSchema.gen[Grouped[T]]
}
