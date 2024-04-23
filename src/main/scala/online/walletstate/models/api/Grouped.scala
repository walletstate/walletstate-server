package online.walletstate.models.api

import online.walletstate.models.{Group, Groupable}
import zio.Chunk
import zio.http.gen.annotations.genericField
import zio.schema.{DeriveSchema, Schema}

final case class Grouped[T <: Groupable](
    id: Group.Id,
    name: String,
    orderingIndex: Int,
    @genericField() items: Chunk[T]
)

object Grouped {

  def apply[T <: Groupable](id: Group.Id, name: String, orderingIndex: Int, items: Seq[T]): Grouped[T] =
    Grouped(id, name, orderingIndex, Chunk.from(items))
  
  given schema[T <: Groupable: Schema]: Schema[Grouped[T]]      = DeriveSchema.gen[Grouped[T]]
}
