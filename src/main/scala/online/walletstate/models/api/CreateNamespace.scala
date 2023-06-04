package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateNamespace(name: String)

object CreateNamespace {
  given codec: JsonCodec[CreateNamespace] = DeriveJsonCodec.gen[CreateNamespace]
}
