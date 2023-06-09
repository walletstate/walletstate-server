package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
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
      ctx      <- ZIO.service[WalletContext]
      info     <- req.as[CreateCategory]
      category <- categoriesService.create(ctx.wallet, info.name, ctx.user)
    } yield Response.json(category.toJson)
  } @@ auth.ctx[WalletContext]

  private val getCategoriesHandler = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx        <- ZIO.service[WalletContext]
      categories <- categoriesService.list(ctx.wallet)
    } yield Response.json(categories.toJson)
  } @@ auth.ctx[WalletContext]

  private def getCategoryHandler(idStr: String) = Handler.fromFunctionZIO[Request] { _ =>
    for {
      ctx      <- ZIO.service[WalletContext]
      id       <- Category.Id.from(idStr)
      category <- categoriesService.get(ctx.wallet, id)
    } yield Response.json(category.toJson)
  } @@ auth.ctx[WalletContext]

  def routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "categories"     => createCategoryHandler
    case Method.GET -> !! / "api" / "categories"      => getCategoriesHandler
    case Method.GET -> !! / "api" / "categories" / id => getCategoryHandler(id)
  }
}

object CategoriesRoutes {
  val layer = ZLayer.fromFunction(CategoriesRoutes.apply _)
}
