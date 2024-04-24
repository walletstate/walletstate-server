package online.walletstate.models.api

import online.walletstate.models.{Group, Groupable}
import zio.Chunk
import zio.http.gen.annotations.genericField
import zio.schema.{DeriveSchema, Schema}

final case class Grouped[T <: Groupable](
    id: Group.Id,
    name: String,
    idx: Int,
    @genericField() items: List[T]
)

object Grouped {
  
  given schema[T <: Groupable: Schema]: Schema[Grouped[T]]      = DeriveSchema.gen[Grouped[T]]
}
