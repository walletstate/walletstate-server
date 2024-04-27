package online.walletstate.services

import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.fixtures.{WalletsFixtures, UsersFixtures}
import online.walletstate.models.User
import online.walletstate.models.AppError.UserNotExist
import zio.*
import zio.test.*
import zio.test.Assertion.*

object UsersServiceSpec extends ZIOSpecDefault with UsersFixtures with WalletsFixtures {

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
            _       <- service.create(NewUserWithWallet)
            user    <- service.get(NewUserWithWallet.id)
          } yield assertTrue(user == NewUserWithWallet)
        }
      ),
      suite("set wallet")(
        test("should set wallet for user") {
          for {
            service    <- ZIO.service[UsersService]
            _          <- service.create(NewUserWithoutWallet)
            userBefore <- service.get(NewUserWithoutWallet.id)
            _          <- service.setWallet(NewUserWithoutWallet.id, ExistingWalletId)
            userAfter  <- service.get(NewUserWithoutWallet.id)
          } yield assertTrue(userBefore.wallet.isEmpty && userAfter.wallet.nonEmpty)
        }
      )
    ) @@ DbMigrationAspect.migrateOnce()()
  }.provideShared(
    UsersServiceLive.layer,
    WalletStateQuillContext.layer,
    ZPostgreSQLContainer.live,
    ZPostgreSQLContainer.Settings.default
  )
}
