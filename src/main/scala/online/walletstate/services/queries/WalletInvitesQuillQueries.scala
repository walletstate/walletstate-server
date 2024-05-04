package online.walletstate.services.queries

import online.walletstate.models.WalletInvite

trait WalletInvitesQuillQueries extends QuillQueries {

  import quill.*
  import io.getquill.*

  protected inline def inviteById(id: WalletInvite.Id): Quoted[EntityQuery[WalletInvite]] =
    quote(query[WalletInvite].filter(_.id == lift(id)))

  protected inline def inviteByCode(code: String): Quoted[EntityQuery[WalletInvite]] =
    quote(query[WalletInvite].filter(_.inviteCode == lift(code)))

  protected inline def insert(invite: WalletInvite): Quoted[Insert[WalletInvite]] =
    quote(query[WalletInvite].insertValue(lift(invite)))

}
