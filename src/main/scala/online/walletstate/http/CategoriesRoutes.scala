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

  private val createCategoryHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      info     <- req.as[CreateCategory]
      category <- categoriesService.create(ctx.wallet, info.name, ctx.user)
    } yield Response.json(category.toJson)
  }

  private val getCategoriesHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      categories <- categoriesService.list(ctx.wallet)
    } yield Response.json(categories.toJson)
  }

  private val getCategoryHandler = Handler.fromFunctionZIO[(Category.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      category <- categoriesService.get(ctx.wallet, id)
    } yield Response.json(category.toJson)
  }

  def routes = Routes(
    Method.POST / "api" / "categories"                   -> auth.walletCtx -> createCategoryHandler,
    Method.GET / "api" / "categories"                    -> auth.walletCtx -> getCategoriesHandler,
    Method.GET / "api" / "categories" / Category.Id.path -> auth.walletCtx -> getCategoryHandler
  )
}

object CategoriesRoutes {
  val layer = ZLayer.fromFunction(CategoriesRoutes.apply _)
}
