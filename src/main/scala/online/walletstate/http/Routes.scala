package online.walletstate.http

import online.walletstate.config.AppConfig
import zio.*
import zio.http.{Http, Request, Response}

class Routes() {

  private val healthRoutes    = new HealthRoutes().routes
  private val namespaceRoutes = new NamespaceRoutes().routes

  val routes = healthRoutes ++ namespaceRoutes

}
