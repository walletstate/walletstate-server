package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateGroup(name: String, orderingIndex: Int)

object CreateGroup {
  given codec: JsonCodec[CreateGroup] = DeriveJsonCodec.gen[CreateGroup]
}
