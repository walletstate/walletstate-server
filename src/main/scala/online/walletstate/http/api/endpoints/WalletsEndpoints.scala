package online.walletstate.http.api.endpoints

import online.walletstate.models.{Wallet, WalletInvite}
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait WalletsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "wallets")
      .in[CreateWallet]
      .out[Wallet](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val getCurrent =
    Endpoint(Method.GET / "api" / "wallets" / "current")
      .out[Wallet]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val createInvite =
    Endpoint(Method.POST / "api" / "wallets" / "invite")
      .out[WalletInvite](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val join =
    Endpoint(Method.POST / "api" / "wallets" / "join")
      .in[JoinWallet]
      .out[Wallet]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val endpoints = Chunk(create, getCurrent, createInvite, join)

  val endpointsMap = Map(
    "create"       -> create,
    "getCurrent"   -> getCurrent,
    "createInvite" -> createInvite,
    "join"         -> join
  )

}

object WalletsEndpoints extends WalletsEndpoints
