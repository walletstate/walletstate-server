package online.walletstate.services.queries

import online.walletstate.models.{User, Wallet}

trait UsersQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def userById(id: User.Id): Quoted[EntityQuery[User]] =
    quote(query[User].filter(_.id == lift(id)))

  protected inline def insertUser(user: User): Quoted[Insert[User]] =
    quote(query[User].insertValue(lift(user)))

  protected inline def updateWallet(user: User.Id, wallet: Wallet.Id): Quoted[Update[User]] =
    quote(userById(user).update(_.wallet -> Some(lift(wallet))))
}
