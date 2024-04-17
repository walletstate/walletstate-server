package online.walletstate.models.api

import online.walletstate.models.{Group, Groupable}
import zio.Chunk
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.schema.{DeriveSchema, Schema}

final case class Grouped[T <: Groupable](id: Group.Id, name: String, orderingIndex: Int, items: Chunk[T])

object Grouped {

  def apply[T <: Groupable](id: Group.Id, name: String, orderingIndex: Int, items: Seq[T]): Grouped[T] =
    Grouped(id, name, orderingIndex, Chunk.from(items))
  
  given codec[T <: Groupable: JsonCodec]: JsonCodec[Grouped[T]] = DeriveJsonCodec.gen[Grouped[T]]
  given schema[T <: Groupable: Schema]: Schema[Grouped[T]] = DeriveSchema.gen[Grouped[T]]
}
