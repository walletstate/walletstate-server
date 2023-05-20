package online.walletstate.http

import zio.ZLayer
import zio.http.*

final case class HealthRoutes() {
  private val health: HttpApp[Any, Nothing] = Http.collect[Request] { case Method.GET -> !! / "health" =>
    Response.text("alive")
  }

  private val version: HttpApp[Any, Nothing] = Http.collect[Request] { case Method.GET -> !! / "version" =>
    Response.text("0.0.1")
  }

  val routes = health ++ version
}

object HealthRoutes {
  val layer = ZLayer.fromFunction(HealthRoutes.apply _)
}
