package online.walletstate.services

import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import online.walletstate.db.QuillNamingStrategy
import online.walletstate.fixtures.NamespaceInvitesFixtures
import online.walletstate.models.namespaces.errors.NamespaceInviteNotExist
import online.walletstate.services.NamespaceInvitesService
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object NamespaceInvitesServiceSpec extends ZIOSpecDefault with NamespaceInvitesFixtures {

  def spec = {
    suite("NamespaceInvitesServiceSpec")(
      suite("save")(
        test("should save a new invite") {
          for {
            service <- ZIO.service[NamespaceInvitesService]
            _       <- service.save(NewInvite)
            invite  <- service.get(NewInvite.inviteCode)
          } yield assertTrue(invite == NewInvite)
        }
      ),
      suite("get")(
        test("should return an invite") {
          for {
            service <- ZIO.service[NamespaceInvitesService]
            invite  <- service.get(ExistingInvite.inviteCode)
          } yield assertTrue(invite == ExistingInvite.copy(validTo = invite.validTo))
        },
        test("should return NamespaceInviteNotExist error") {
          for {
            service <- ZIO.service[NamespaceInvitesService]
            res     <- service.get("SOMENONEXISTINGCODE").exit
          } yield assert(res)(fails(equalTo(NamespaceInviteNotExist)))
        }
      ),
      suite("delete")(
        test("should remove the invite") {
          for {
            service <- ZIO.service[NamespaceInvitesService]
            _       <- service.save(AnotherNewInvite)
            invite  <- service.get(AnotherNewInvite.inviteCode)
            _       <- service.delete(AnotherNewInvite.id)
            res     <- service.get(AnotherNewInvite.inviteCode).exit
          } yield assertTrue(invite == AnotherNewInvite) && assert(res)(fails(equalTo(NamespaceInviteNotExist)))
        }
      )
    ) @@ DbMigrationAspect.migrateOnce()()
  }.provideShared(
    NamespaceInvitesServiceLive.layer,
    Quill.Postgres.fromNamingStrategy(QuillNamingStrategy),
    ZPostgreSQLContainer.live,
    ZPostgreSQLContainer.Settings.default
  )

}
