package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateAccountsGroup(name: String, orderingIndex: Int)

object CreateAccountsGroup {
  given codec: JsonCodec[CreateAccountsGroup] = DeriveJsonCodec.gen[CreateAccountsGroup]
}
