package online.walletstate.http

import zio.ZLayer
import zio.http.*

final case class HealthRoutes() {
  private val health = Routes(Method.GET / "health" -> handler(Response.text("alive")))

  private val version = Routes(Method.GET / "version" -> handler(Response.text("0.0.1")))

  val routes = health ++ version
}

object HealthRoutes {
  val layer = ZLayer.fromFunction(HealthRoutes.apply _)
}
