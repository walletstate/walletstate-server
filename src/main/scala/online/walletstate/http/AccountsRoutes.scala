package online.walletstate.http

import online.walletstate.http.endpoints.AccountsEndpoints
import online.walletstate.models.HttpError
import online.walletstate.services.{AccountsService, RecordsService}
import zio.*
import zio.http.*

case class AccountsRoutes(accountsService: AccountsService, recordsService: RecordsService)
    extends WalletStateRoutes
    with AccountsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(accInfo => accountsService.create(accInfo))
  }

  private val listRoute = list.implement {
    Handler.fromFunctionZIO(_ => accountsService.list)
  }

  private val listGroupedRoute = listGrouped.implement {
    Handler.fromFunctionZIO(_ => accountsService.grouped)
  }

  private val getRoute = get.implement {
    Handler.fromFunctionZIO(id => accountsService.get(id).mapError(HttpError.NotFound.apply))
  }

  private val updateRoute = update.implement {
    Handler.fromFunctionZIO((id, info) => accountsService.update(id, info))
  }

  private val listRecordsRoute = listRecords.implement {
    Handler.fromFunctionZIO((id, token) => recordsService.list(id, token))
  }

  private val getBalanceRoute = getBalance.implement {
    Handler.fromFunctionZIO(id => recordsService.balance(id))
  }

  override val walletRoutes = Routes(
    createRoute,
    listGroupedRoute,
    getRoute,
    updateRoute,
    listRoute,
    listRecordsRoute,
    getBalanceRoute
  )
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
