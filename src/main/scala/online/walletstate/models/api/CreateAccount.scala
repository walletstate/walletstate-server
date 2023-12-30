package online.walletstate.models.api

import online.walletstate.models.Group
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class CreateAccount(group: Group.Id, name: String, orderingIndex: Int, icon: String, tags: Seq[String])

object CreateAccount {
  given codec: JsonCodec[CreateAccount] = DeriveJsonCodec.gen[CreateAccount]
}
