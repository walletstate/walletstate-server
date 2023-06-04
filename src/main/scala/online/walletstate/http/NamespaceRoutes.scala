package online.walletstate.http

import online.walletstate.http.auth.*
import online.walletstate.http.auth.AuthCookiesOps.withAuthCookies
import online.walletstate.models.Namespace
import online.walletstate.models.api.{CreateNamespace, JoinNamespace}
import online.walletstate.services.*
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.http.endpoint.*
import zio.json.*

final case class NamespaceRoutes(
    auth: AuthMiddleware,
    namespaceService: NamespacesService,
    tokenService: TokenService
) {

  private val createNamespaceHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx       <- ZIO.service[UserContext]
      nsInfo    <- req.as[CreateNamespace]
      namespace <- namespaceService.create(ctx.user, nsInfo.name)
      newToken  <- tokenService.encode(UserNamespaceContext(ctx.user, namespace.id))
    } yield Response.json(namespace.toJson).withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  private val getCurrentNamespaceHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx <- ZIO.service[UserNamespaceContext]
      ns  <- namespaceService.get(ctx.namespace)
    } yield Response.json(ns.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private val inviteNamespaceHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx    <- ZIO.service[UserNamespaceContext]
      invite <- namespaceService.createInvite(ctx.user, ctx.namespace)
    } yield Response.json(invite.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private val joinNamespaceHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx       <- ZIO.service[UserContext]
      joinInfo  <- req.as[JoinNamespace]
      namespace <- namespaceService.joinNamespace(ctx.user, joinInfo.inviteCode)
      newToken  <- tokenService.encode(UserNamespaceContext(ctx.user, namespace.id))
    } yield Response.ok.withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  val routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "namespaces"            => createNamespaceHandler
    case Method.GET -> !! / "api" / "namespaces" / "current" => getCurrentNamespaceHandler
    case Method.POST -> !! / "api" / "namespaces" / "invite" => inviteNamespaceHandler
    case Method.POST -> !! / "api" / "namespaces" / "join"   => joinNamespaceHandler
  }
}

object NamespaceRoutes {
  val layer = ZLayer.fromFunction(NamespaceRoutes.apply _)
}
