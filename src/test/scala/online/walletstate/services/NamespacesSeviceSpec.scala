package online.walletstate.services

import io.getquill.jdbczio.Quill
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import online.walletstate.db.QuillNamingStrategy
import online.walletstate.fixtures.{NamespacesFixtures, UsersFixtures}
import online.walletstate.models.Namespace
import online.walletstate.models.errors.{NamespaceInviteNotExist, NamespaceNotExist, UserAlreadyHasNamespace}
import online.walletstate.services.{NamespacesService, NamespacesServiceLive}
import zio.*
import zio.test.*
import zio.test.Assertion.*

import java.util.UUID

object NamespacesSeviceSpec extends ZIOSpecDefault with NamespacesFixtures with UsersFixtures {

  def spec = {
    suite("NamespacesSeviceSpec")(
      suite("get")(
        test("should return existing namespace") {
          for {
            service   <- ZIO.service[NamespacesService]
            namespace <- service.get(ExistingNamespaceId)
          } yield assertTrue(namespace == ExistingNamespace)
        },
        test("should return NamespaceNotExist error") {
          for {
            service  <- ZIO.service[NamespacesService]
            randomId <- Namespace.Id.random
            res      <- service.get(randomId).exit
          } yield assert(res)(fails(equalTo(NamespaceNotExist)))
        }
      ),
      suite("create")(
        test("should create a new namespace") {
          for {
            service   <- ZIO.service[NamespacesService]
            namespace <- service.create(ExistingUserWithoutNamespaceId1, "test-namespace-1")
          } yield assertTrue(namespace.name == "test-namespace-1")
        },
        test("should return UserAlreadyHasNamespace error") {
          for {
            service <- ZIO.service[NamespacesService]
            res     <- service.create(ExistingUserId, "test-namespace-1").exit
          } yield assert(res)(fails(equalTo(UserAlreadyHasNamespace)))
        }
      ),
      suite("create invite")(
        test("should create a new invite") {
          for {
            _       <- TestRandom.feedUUIDs(UUID.randomUUID())
            service <- ZIO.service[NamespacesService]
            invite  <- service.createInvite(ExistingUserId, ExistingNamespaceId)
          } yield assertTrue(invite.inviteCode.nonEmpty)
        }
      ),
      suite("join namespace")(
        test("should successfully join user to namespace") {
          for {
            nsService    <- ZIO.service[NamespacesService]
            usersService <- ZIO.service[UsersService]
            _            <- TestRandom.feedUUIDs(UUID.randomUUID())
            invite       <- nsService.createInvite(ExistingUserId, ExistingNamespaceId)
            userBefore   <- usersService.get(ExistingUserWithoutNamespaceId2)
            _            <- nsService.joinNamespace(ExistingUserWithoutNamespaceId2, invite.inviteCode)
            userAfter    <- usersService.get(ExistingUserWithoutNamespaceId2)
          } yield assertTrue(userBefore.namespace.isEmpty && userAfter.namespace.contains(ExistingNamespaceId))
        },
        test("should return an error code is invalid") {
          for {
            nsService    <- ZIO.service[NamespacesService]
            usersService <- ZIO.service[UsersService]
            _            <- TestRandom.feedUUIDs(UUID.randomUUID())
            invite       <- nsService.createInvite(ExistingUserId, ExistingNamespaceId)
            res          <- nsService.joinNamespace(ExistingUserWithoutNamespaceId3, "INVALIDCODE").exit
            user         <- usersService.get(ExistingUserWithoutNamespaceId3)
          } yield assertTrue(user.namespace.isEmpty) && assert(res)(fails(equalTo(NamespaceInviteNotExist)))
        }
        // TODO Add test cases for invitation expiration
      )
    ) @@ DbMigrationAspect.migrateOnce()()
  }.provideShared(
    NamespacesServiceLive.layer,
    NamespaceInvitesServiceLive.layer,
    UsersServiceLive.layer,
    Quill.Postgres.fromNamingStrategy(QuillNamingStrategy),
    ZPostgreSQLContainer.live,
    ZPostgreSQLContainer.Settings.default
  )
}
