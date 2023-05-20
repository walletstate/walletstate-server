package online.walletstate.repos

import online.walletstate.domain.User
import online.walletstate.domain.errors.UserNotFound
import zio.*

import scala.collection.concurrent.TrieMap

trait UsersRepo {
  def save(user: User): Task[Unit]

  def get(userId: String): Task[User]
}

final case class InMemoryUsersRepo() extends UsersRepo {
  private val storage = TrieMap.empty[String, User]

  override def save(user: User) = ZIO.succeed {
    storage.put(user.id, user)
  }

  override def get(userId: String) =
    ZIO.fromOption(storage.get(userId)).mapError(_ => UserNotFound)
}

object InMemoryUsersRepo {
  val layer: ULayer[UsersRepo] = ZLayer.succeed(InMemoryUsersRepo())
}