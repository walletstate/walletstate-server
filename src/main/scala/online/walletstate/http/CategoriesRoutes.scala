package online.walletstate.http

import online.walletstate.http.api.CategoriesEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.Category
import online.walletstate.models.api.CreateCategory
import online.walletstate.services.CategoriesService
import zio.*
import zio.http.*

case class CategoriesRoutes(auth: AuthMiddleware, categoriesService: CategoriesService) extends CategoriesEndpoints {
  import auth.implementWithWalletCtx
  
  private val createRoute = create.implementWithWalletCtx[(CreateCategory, WalletContext)] {
    Handler.fromFunctionZIO((info, ctx) => categoriesService.create(ctx.wallet, ctx.user, info))
  }()

  private val listRoute = list.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => categoriesService.list(ctx.wallet))
  }()

  private val listGroupedRoute = listGrouped.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => categoriesService.grouped(ctx.wallet))
  }()

  private val getRoute = get.implementWithWalletCtx[(Category.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => categoriesService.get(ctx.wallet, id))
  }()

  val routes = Routes(createRoute, listRoute, listGroupedRoute, getRoute)
}

object CategoriesRoutes {
  val layer = ZLayer.fromFunction(CategoriesRoutes.apply _)
}
