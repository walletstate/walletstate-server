package online.walletstate.models.api

import online.walletstate.models.AccountsGroup
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateAccount(group: AccountsGroup.Id, name: String, orderingIndex: Int, icon: String)

object CreateAccount {
  given codec: JsonCodec[CreateAccount] = DeriveJsonCodec.gen[CreateAccount]
}
