package online.walletstate.http

import online.walletstate.domain.{CreateNamespace, JoinNamespace, Namespace}
import online.walletstate.http.RequestOps.as
import online.walletstate.http.auth.*
import online.walletstate.http.auth.AuthCookiesOps.withAuthCookies
import online.walletstate.services.*
import online.walletstate.services.auth.TokenService
import zio.http.*
import zio.http.endpoint.*
import zio.json.*
import zio.*

import java.util.UUID

final case class NamespaceRoutes(
    auth: AuthMiddleware,
    namespaceService: NamespaceService,
    tokenService: TokenService,
    usersService: UsersService
) {

  private val createNamespaceHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[UserContext]
      now      <- Clock.instant
      _        <- ZIO.logInfo(s"Context $ctx")
      ns       <- req.as[CreateNamespace]
      uuid     <- namespaceService.create(Namespace(UUID.randomUUID(), ns.name, ctx.user, now))
      user     <- usersService.get(ctx.user)
      _        <- usersService.save(user.copy(namespace = Some(uuid)))
      newToken <- tokenService.encode(UserNamespaceContext(ctx.user, uuid))
    } yield Response.text(uuid.toString).withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  private val getNamespaceHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx <- ZIO.service[UserNamespaceContext]
      _   <- ZIO.logInfo(s"Context $ctx")
      ns <- namespaceService.get(ctx.namespace).map {
        case Some(ns) => Response.json(ns.toJson)
        case None     => Response.status(Status.NotFound)
      }
    } yield ns
  } @@ auth.ctx[UserNamespaceContext]

  private val joinNamespaceHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[UserContext]
      joinInfo <- req.as[JoinNamespace]
      user     <- usersService.get(ctx.user)
      _        <- usersService.save(user.copy(namespace = Some(joinInfo.namespace)))
      newToken <- tokenService.encode(UserNamespaceContext(ctx.user, joinInfo.namespace))
    } yield Response.ok.withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  private val createNamespaceRoute =
    Http.collectHandler[Request] { case Method.POST -> !! / "namespace" => createNamespaceHandler }

  private val getNamespaceRoute =
    Http.collectHandler[Request] { case Method.GET -> !! / "namespace" => getNamespaceHandler }

  private val joinNamespaceRoute =
    Http.collectHandler[Request] { case Method.POST -> !! / "namespace" / "join" => joinNamespaceHandler }

  val routes = createNamespaceRoute ++ getNamespaceRoute ++ joinNamespaceRoute
}

object NamespaceRoutes {
  val layer = ZLayer.fromFunction(NamespaceRoutes.apply _)
}
