package online.walletstate.http

import online.walletstate.domain.Namespace
import online.walletstate.services.NamespaceService
import zio.http.*

import java.util.UUID

class NamespaceRoutes() {

  private val createNamespaceRoute = Http.collectZIO[Request] { case Method.POST -> !! / "namespace" =>
    val uuid = UUID.randomUUID()
    NamespaceService
      .create(Namespace(uuid, "req.body.asString"))
      .map(rs => Response.text(uuid.toString))
  }

  private val getNamespaceRoute = Http.collectZIO[Request] { case Method.GET -> !! / "namespace" / uuid =>
    NamespaceService.get(UUID.fromString(uuid)).map {
      case Some(ns) => Response.json(s"{\"id\": \"${ns.id}\", \"name\": \"${ns.name}\"}")
      case None     => Response.status(Status.NotFound)
    }
  }

  val routes = createNamespaceRoute ++ getNamespaceRoute
}
