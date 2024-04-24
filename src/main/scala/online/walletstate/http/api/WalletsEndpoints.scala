package online.walletstate.http.api

import online.walletstate.models.{AppError, Wallet, WalletInvite}
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait WalletsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "wallets")
      .in[CreateWallet]
      .out[Wallet](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val getCurrent =
    Endpoint(Method.GET / "api" / "wallets" / "current")
      .out[Wallet]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      //TODO workaround: need to specify `ErrorBody` directly at least once.
      //In other case Swagger generator doesn't generate schema
      .outError[AppError.ErrorBody](Status.Custom(999))

  val createInvite =
    Endpoint(Method.POST / "api" / "wallets" / "invite")
      .out[WalletInvite](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val join =
    Endpoint(Method.POST / "api" / "wallets" / "join")
      .in[JoinWallet]
      .out[Wallet]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.WalletInviteNotExist.type](Status.NotFound)
      .outError[AppError.WalletInviteExpired.type](Status.Forbidden)

  val endpointsMap = Map(
    "create"       -> create,
    "getCurrent"   -> getCurrent,
    "createInvite" -> createInvite,
    "join"         -> join
  )

  val endpoints = endpointsMap.values
}

object WalletsEndpoints extends WalletsEndpoints
