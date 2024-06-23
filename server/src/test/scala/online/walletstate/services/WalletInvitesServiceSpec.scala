package online.walletstate.services

import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.fixtures.WalletInvitesFixtures
import online.walletstate.models.AppError.WalletInviteNotExist
import online.walletstate.services.WalletInvitesService
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object WalletInvitesServiceSpec extends ZIOSpecDefault with WalletInvitesFixtures {

  def spec = {
    suite("WalletInvitesServiceSpec")(
      suite("save")(
        test("should save a new invite") {
          for {
            service <- ZIO.service[WalletInvitesService]
            _       <- service.save(NewInvite)
            invite  <- service.get(NewInvite.inviteCode)
          } yield assertTrue(invite == NewInvite)
        }
      ),
      suite("get")(
        test("should return an invite") {
          for {
            service <- ZIO.service[WalletInvitesService]
            invite  <- service.get(ExistingInvite.inviteCode)
          } yield assertTrue(invite == ExistingInvite.copy(validTo = invite.validTo))
        },
        test("should return WalletInviteNotExist error") {
          for {
            service <- ZIO.service[WalletInvitesService]
            res     <- service.get("SOMENONEXISTINGCODE").exit
          } yield assert(res)(fails(equalTo(WalletInviteNotExist)))
        }
      ),
      suite("delete")(
        test("should remove the invite") {
          for {
            service <- ZIO.service[WalletInvitesService]
            _       <- service.save(AnotherNewInvite)
            invite  <- service.get(AnotherNewInvite.inviteCode)
            _       <- service.delete(AnotherNewInvite.id)
            res     <- service.get(AnotherNewInvite.inviteCode).exit
          } yield assertTrue(invite == AnotherNewInvite) && assert(res)(fails(equalTo(WalletInviteNotExist)))
        }
      )
    ) @@ DbMigrationAspect.migrateOnce()()
  }.provideShared(
    WalletInvitesServiceLive.layer,
    WalletStateQuillContext.layer,
    ZPostgreSQLContainer.live,
    ZPostgreSQLContainer.Settings.default
  )

}
