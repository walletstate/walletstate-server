package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, UserNamespaceContext}
import online.walletstate.models.Category
import online.walletstate.models.api.CreateCategory
import online.walletstate.services.CategoriesService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class CategoriesRoutes(auth: AuthMiddleware, categoriesService: CategoriesService) {

  private val createCategoryHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx      <- ZIO.service[UserNamespaceContext]
      info     <- req.as[CreateCategory]
      category <- categoriesService.create(ctx.namespace, info.name, ctx.user)
    } yield Response.json(category.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private val getCategoriesHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx        <- ZIO.service[UserNamespaceContext]
      categories <- categoriesService.list(ctx.namespace)
    } yield Response.json(categories.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private def getCategoryHandler(idStr: String) = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx      <- ZIO.service[UserNamespaceContext]
      id       <- Category.Id.from(idStr)
      category <- categoriesService.get(ctx.namespace, id)
    } yield Response.json(category.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  def routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "categories"     => createCategoryHandler
    case Method.GET -> !! / "api" / "categories"      => getCategoriesHandler
    case Method.GET -> !! / "api" / "categories" / id => getCategoryHandler(id)
  }
}

object CategoriesRoutes {
  val layer = ZLayer.fromFunction(CategoriesRoutes.apply _)
}
