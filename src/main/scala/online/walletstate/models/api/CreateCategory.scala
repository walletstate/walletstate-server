package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateCategory(name: String)

object CreateCategory {
  given codec: JsonCodec[CreateCategory] = DeriveJsonCodec.gen[CreateCategory]
}
