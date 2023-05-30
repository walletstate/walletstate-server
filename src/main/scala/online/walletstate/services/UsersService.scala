package online.walletstate.services

import online.walletstate.domain.users.User
import online.walletstate.repos.UsersRepo
import zio.{Task, ZLayer}

trait UsersService {
  def create(user: User): Task[User]
  def get(userId: String): Task[User]
}

final case class UsersServiceLive(usersRepo: UsersRepo) extends UsersService {
  override def create(user: User): Task[User] = usersRepo.save(user)

  override def get(userId: String): Task[User] = usersRepo.get(userId)
}

object UsersServiceLive {
  val layer = ZLayer.fromFunction(UsersServiceLive.apply _)
}
