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
    tokenService: TokenService
) {

  private val createNamespaceHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx       <- ZIO.service[UserContext]
      nsInfo    <- req.as[CreateNamespace]
      namespace <- namespaceService.create(ctx.user, nsInfo)
      newToken  <- tokenService.encode(UserNamespaceContext(ctx.user, namespace.id))
    } yield Response.json(namespace.toJson).withAuthCookies(newToken)
  } @@ auth.ctx[UserContext]

  private val getNamespaceHandler = Handler.fromFunctionZIO[Request] { _ =>
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

  private val createNamespaceRoute =
    Http.collectHandler[Request] { case Method.POST -> !! / "namespace" => createNamespaceHandler }

  private val getNamespaceRoute =
    Http.collectHandler[Request] { case Method.GET -> !! / "namespace" => getNamespaceHandler }

  private val inviteNamespaceRoute =
    Http.collectHandler[Request] { case Method.POST -> !! / "namespace" / "invite" => inviteNamespaceHandler }

  private val joinNamespaceRoute =
    Http.collectHandler[Request] { case Method.POST -> !! / "namespace" / "join" => joinNamespaceHandler }

  val routes = createNamespaceRoute ++ getNamespaceRoute ++ inviteNamespaceRoute ++ joinNamespaceRoute
}

object NamespaceRoutes {
  val layer = ZLayer.fromFunction(NamespaceRoutes.apply _)
}
