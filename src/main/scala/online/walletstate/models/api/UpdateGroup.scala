package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class UpdateGroup(name: String)

object UpdateGroup {
  given codec: JsonCodec[UpdateGroup] = DeriveJsonCodec.gen[UpdateGroup]
}
