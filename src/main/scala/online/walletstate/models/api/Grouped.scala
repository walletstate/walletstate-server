package online.walletstate.models.api

import online.walletstate.models.{Group, Groupable}
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class Grouped[T <: Groupable](id: Group.Id, name: String, orderingIndex: Int, items: Seq[T])

object Grouped {
  given codec[T <: Groupable: JsonCodec]: JsonCodec[Grouped[T]] = DeriveJsonCodec.gen[Grouped[T]]
}
