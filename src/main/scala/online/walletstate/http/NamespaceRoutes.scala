package online.walletstate.http

import online.walletstate.domain.namespaces.codecs.given
import online.walletstate.domain.namespaces.{CreateNamespace, JoinNamespace, Namespace}
import online.walletstate.http.RequestOps.as
import online.walletstate.http.auth.*
import online.walletstate.http.auth.AuthCookiesOps.withAuthCookies
import online.walletstate.services.*
import online.walletstate.services.auth.TokenService
import zio.*
import zio.http.*
import zio.http.endpoint.*
import zio.json.*

import java.util.UUID

final case class NamespaceRoutes(
    auth: AuthMiddleware,
    namespaceService: NamespacesService,
    tokenService: TokenService,
    usersService: UsersService
) {

  private val createNamespaceHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx       <- ZIO.service[UserContext]
      now       <- Clock.instant
      nsInfo    <- req.as[CreateNamespace]
      namespace <- namespaceService.create(Namespace(UUID.randomUUID(), nsInfo.name, ctx.user, now))
      user      <- usersService.get(ctx.user)
      _         <- usersService.save(user.copy(namespace = Some(namespace.id)))
      newToken  <- tokenService.encode(UserNamespaceContext(ctx.user, namespace.id))
    } yield Response.json(namespace.toJson).withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  private val getNamespaceHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx <- ZIO.service[UserNamespaceContext]
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
