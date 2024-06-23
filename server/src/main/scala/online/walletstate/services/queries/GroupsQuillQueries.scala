package online.walletstate.services.queries

import online.walletstate.common.models.{Group, Wallet}

trait GroupsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(accountsGroup: Group): Quoted[Insert[Group]] =
    quote(query[Group].insertValue(lift(accountsGroup)))

  protected inline def groupsByWallet(wallet: Wallet.Id): Quoted[EntityQuery[Group]] =
    quote(query[Group].filter(_.wallet == lift(wallet)))

  protected inline def groupsByType(wallet: Wallet.Id, `type`: Group.Type): Quoted[EntityQuery[Group]] =
    quote(groupsByWallet(wallet).filter(_.`type` == lift(`type`)))

  protected inline def groupsById(wallet: Wallet.Id, group: Group.Id): EntityQuery[Group] =
    groupsByWallet(wallet).filter(_.id == lift(group))

  protected inline def updateQuery(wallet: Wallet.Id, group: Group.Id, name: String, idx: Int): Update[Group] =
    groupsById(wallet, group).update(_.name -> lift(name), _.idx -> lift(idx))

  protected inline def deleteQuery(wallet: Wallet.Id, group: Group.Id): Delete[Group] =
    groupsById(wallet, group).delete
}
