package online.walletstate.repos

import online.walletstate.domain.users.User
import online.walletstate.domain.users.errors.UserNotExists
import zio.*

import scala.collection.concurrent.TrieMap

trait UsersRepo {
  def save(user: User): Task[User]

  def get(userId: String): Task[User]
}

final case class InMemoryUsersRepo() extends UsersRepo {
  private val storage = TrieMap.empty[String, User]

  override def save(user: User): Task[User] = ZIO.succeed {
    storage.put(user.id, user)
    user
  }

  override def get(userId: String): Task[User] =
    ZIO.fromOption(storage.get(userId)).mapError(_ => UserNotExists)
}

object InMemoryUsersRepo {
  val layer: ULayer[UsersRepo] = ZLayer.succeed(InMemoryUsersRepo())
}
