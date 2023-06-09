package online.walletstate.services

import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import online.walletstate.db.QuillNamingStrategy
import online.walletstate.fixtures.{WalletsFixtures, UsersFixtures}
import online.walletstate.models.Wallet
import online.walletstate.models.errors.{WalletInviteNotExist, WalletNotExist, UserAlreadyHasWallet}
import online.walletstate.services.{WalletsService, WalletsServiceLive}
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object WalletsSeviceSpec extends ZIOSpecDefault with WalletsFixtures with UsersFixtures {

  def spec = {
    suite("WalletsSeviceSpec")(
      suite("get")(
        test("should return existing wallet") {
          for {
            service   <- ZIO.service[WalletsService]
            wallet <- service.get(ExistingWalletId)
          } yield assertTrue(wallet == ExistingWallet)
        },
        test("should return WalletNotExist error") {
          for {
            service  <- ZIO.service[WalletsService]
            randomId <- Wallet.Id.random
            res      <- service.get(randomId).exit
          } yield assert(res)(fails(equalTo(WalletNotExist)))
        }
      ),
      suite("create")(
        test("should create a new wallet") {
          for {
            service   <- ZIO.service[WalletsService]
            wallet <- service.create(ExistingUserWithoutWalletId1, "test-wallet-1")
          } yield assertTrue(wallet.name == "test-wallet-1")
        },
        test("should return UserAlreadyHasWallet error") {
          for {
            service <- ZIO.service[WalletsService]
            res     <- service.create(ExistingUserId, "test-wallet-1").exit
          } yield assert(res)(fails(equalTo(UserAlreadyHasWallet)))
        }
      ),
      suite("create invite")(
        test("should create a new invite") {
          for {
            _       <- TestRandom.feedUUIDs(UUID.randomUUID())
            service <- ZIO.service[WalletsService]
            invite  <- service.createInvite(ExistingUserId, ExistingWalletId)
          } yield assertTrue(invite.inviteCode.nonEmpty)
        }
      ),
      suite("join wallet")(
        test("should successfully join user to wallet") {
          for {
            nsService    <- ZIO.service[WalletsService]
            usersService <- ZIO.service[UsersService]
            _            <- TestRandom.feedUUIDs(UUID.randomUUID())
            invite       <- nsService.createInvite(ExistingUserId, ExistingWalletId)
            userBefore   <- usersService.get(ExistingUserWithoutWalletId2)
            _            <- nsService.joinWallet(ExistingUserWithoutWalletId2, invite.inviteCode)
            userAfter    <- usersService.get(ExistingUserWithoutWalletId2)
          } yield assertTrue(userBefore.wallet.isEmpty && userAfter.wallet.contains(ExistingWalletId))
        },
        test("should return an error code is invalid") {
          for {
            nsService    <- ZIO.service[WalletsService]
            usersService <- ZIO.service[UsersService]
            _            <- TestRandom.feedUUIDs(UUID.randomUUID())
            invite       <- nsService.createInvite(ExistingUserId, ExistingWalletId)
            res          <- nsService.joinWallet(ExistingUserWithoutWalletId3, "INVALIDCODE").exit
            user         <- usersService.get(ExistingUserWithoutWalletId3)
          } yield assertTrue(user.wallet.isEmpty) && assert(res)(fails(equalTo(WalletInviteNotExist)))
        }
        // TODO Add test cases for invitation expiration
      )
    ) @@ DbMigrationAspect.migrateOnce()()
  }.provideShared(
    WalletsServiceLive.layer,
    WalletInvitesServiceLive.layer,
    UsersServiceLive.layer,
    Quill.Postgres.fromNamingStrategy(QuillNamingStrategy),
    ZPostgreSQLContainer.live,
    ZPostgreSQLContainer.Settings.default
  )
}
