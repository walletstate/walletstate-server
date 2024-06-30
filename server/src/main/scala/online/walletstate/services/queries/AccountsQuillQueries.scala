package online.walletstate.services.queries

import online.walletstate.common.models.{Account, Group, Wallet}

trait AccountsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(account: Account): Quoted[Insert[Account]] = quote(
    query[Account].insertValue(lift(account))
  )

  protected inline def accountsByWallet(wallet: Wallet.Id): Quoted[Query[Account]] = quote {
    query[Account]
      .join(query[Group])
      .on(_.group == _.id)
      .filter { case (_, group) => group.wallet == lift(wallet) }
      .map { case (account, _) => account }
  }

  protected inline def accountsByGroup(wallet: Wallet.Id, group: Group.Id): Query[Account] =
    accountsByWallet(wallet).filter(_.group == lift(group))

  protected inline def accountsById(wallet: Wallet.Id, id: Account.Id): Query[Account] =
    accountsByWallet(wallet).filter(_.id == lift(id))

  protected inline def updateQuery(id: Account.Id, info: Account.Data): Update[Account] =
    Tables.Accounts
      .filter(_.id == lift(id))
      .update(
        _.group        -> lift(info.group),
        _.name         -> lift(info.name),
        _.defaultAsset -> lift(info.defaultAsset),
        _.icon         -> lift(info.icon),
        _.tags         -> lift(info.tags),
        _.idx          -> lift(info.idx),
        _.externalId   -> lift(info.externalId)
      )
}
