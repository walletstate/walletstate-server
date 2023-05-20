package online.walletstate.services

import online.walletstate.domain.User
import online.walletstate.repos.UsersRepo
import zio.{Task, ZLayer}

trait UsersService {

  def save(user: User): Task[User]
  def get(userId: String): Task[User]

}

final case class UsersServiceImpl(usersRepo: UsersRepo) extends UsersService {
  override def save(user: User): Task[User] =
    usersRepo.save(user).map(_ => user)

  override def get(userId: String): Task[User] =
    usersRepo.get(userId)
}

object UsersServiceImpl {
  val layer = ZLayer.fromFunction(UsersServiceImpl.apply _)
}
