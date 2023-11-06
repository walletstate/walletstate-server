package online.walletstate.models.api

import zio.json.{DeriveJsonCodec, JsonCodec}

final case class UpdateAccountsGroup(name: String)

object UpdateAccountsGroup {
  given codec: JsonCodec[UpdateAccountsGroup] = DeriveJsonCodec.gen[UpdateAccountsGroup]
}
