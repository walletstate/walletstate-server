package online.walletstate.models.api

import online.walletstate.models.{Account, AccountsGroup}
import zio.json.{DeriveJsonCodec, JsonCodec}

final case class SimpleAccount(
    id: Account.Id,
    name: String,
    orderingIndex: Int,
    icon: String,
    tags: Seq[String] = Seq("tag1", "tag2")
)

object SimpleAccount {

  def fromAccount(account: Account) = SimpleAccount(
    account.id,
    account.name,
    account.orderingIndex,
    account.icon
  )

  given codec: JsonCodec[SimpleAccount] = DeriveJsonCodec.gen[SimpleAccount]
}

final case class AccountsGroupWithAccounts(
    id: AccountsGroup.Id,
    name: String,
    orderingIndex: Int,
    accounts: Seq[SimpleAccount]
)

object AccountsGroupWithAccounts {

  def build(group: AccountsGroup, accounts: Seq[SimpleAccount]) =
    AccountsGroupWithAccounts(
      group.id,
      group.name,
      group.orderingIndex,
      accounts
    )

  given codec: JsonCodec[AccountsGroupWithAccounts] = DeriveJsonCodec.gen[AccountsGroupWithAccounts]
}
