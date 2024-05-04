package online.walletstate.services.queries

import online.walletstate.models.{User, Wallet, WalletUser}

trait WalletsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(wallet: Wallet): Insert[Wallet] =
    Tables.Wallets.insertValue(lift(wallet))

  protected inline def walletById(id: Wallet.Id): EntityQuery[Wallet] =
    Tables.Wallets.filter(_.id == lift(id))

  protected inline def insertWalletUser(walletUser: WalletUser): Insert[WalletUser] =
    Tables.WalletUsers.insertValue(lift(walletUser))

  protected inline def userExists(wallet: Wallet.Id, user: User.Id): Boolean =
    Tables.WalletUsers.filter(_.wallet == lift(wallet)).filter(_.user == lift(user)).nonEmpty
}
