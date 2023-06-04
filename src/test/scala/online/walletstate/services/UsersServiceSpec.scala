package online.walletstate.services

import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import online.walletstate.db.QuillNamingStrategy
import online.walletstate.fixtures.{NamespacesFixtures, UsersFixtures}
import online.walletstate.models.User
import online.walletstate.models.errors.UserNotExist
import zio.*
import zio.test.*
import zio.test.Assertion.*

object UsersServiceSpec extends ZIOSpecDefault with UsersFixtures with NamespacesFixtures {

  def spec = {
    suite("UsersServiceSpec ")(
      suite("get user ")(
        test("should return a user if exists") {
          for {
            service <- ZIO.service[UsersService]
            user    <- service.get(ExistingUser.id)
          } yield assertTrue(user == ExistingUser)
        },
        test("should return UserNotExist error if user doesn't exist") {
          for {
            service <- ZIO.service[UsersService]
            res     <- service.get(User.Id("not-existing-user-id")).exit
          } yield assert(res)(fails(equalTo(UserNotExist)))
        }
      ),
      suite("create")(
        test("should create a user") {
          for {
            service <- ZIO.service[UsersService]
            _       <- service.create(NewUserWithNamespace)
            user    <- service.get(NewUserWithNamespace.id)
          } yield assertTrue(user == NewUserWithNamespace)
        }
      ),
      suite("set namespace")(
        test("should set namespace for user") {
          for {
            service    <- ZIO.service[UsersService]
            _          <- service.create(NewUserWithoutNamespace)
            userBefore <- service.get(NewUserWithoutNamespace.id)
            _          <- service.setNamespace(NewUserWithoutNamespace.id, ExistingNamespaceId)
            userAfter  <- service.get(NewUserWithoutNamespace.id)
          } yield assertTrue(userBefore.namespace.isEmpty && userAfter.namespace.nonEmpty)
        }
      )
    ) @@ DbMigrationAspect.migrateOnce()()
  }.provideShared(
    UsersServiceLive.layer,
    Quill.Postgres.fromNamingStrategy(QuillNamingStrategy),
    ZPostgreSQLContainer.live,
    ZPostgreSQLContainer.Settings.default
  )
}
