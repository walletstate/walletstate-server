package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.*
import online.walletstate.common.models.{AuthToken, Wallet, WalletInvite}
import zio.http.codec.{Doc, HttpCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Method, Status}

trait WalletsEndpoints extends WalletStateEndpoints {

  val createEndpoint =
    Endpoint(Method.POST / "api" / "wallets")
      .withAuth
      .in[Wallet.Data]
      .out[Wallet](Status.Created)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound) ?? Doc.p("Something strange. User not found"),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val getCurrentEndpoint =
    Endpoint(Method.GET / "api" / "wallets" / "current").withBadRequestCodec
      .withAuth
      .out[Wallet]
      .outErrors[Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound) ?? Doc.p("Wallet not found"),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val createInviteEndpoint =
    Endpoint(Method.POST / "api" / "wallets" / "invite")
      .withAuth
      .out[WalletInvite](Status.Created)
      .outErrors[Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound) ?? Doc.p("Wallet not found"),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val joinEndpoint =
    Endpoint(Method.POST / "api" / "wallets" / "join")
      .withAuth
      .in[Wallet.Join]
      .out[Wallet]
      .outErrors[BadRequest | Unauthorized | Forbidden | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[Forbidden](Status.Forbidden) ?? Doc.p("Invite expired"),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val createApiToken =
    Endpoint(Method.POST / "api" / "wallets" / "token")
      .withAuth
      .in[AuthToken.Create]
      .out[AuthToken](Status.Created)
      .outErrors[BadRequest | Unauthorized | Forbidden | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[Forbidden](Status.Forbidden),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create"         -> createEndpoint,
    "getCurrent"     -> getCurrentEndpoint,
    "createInvite"   -> createInviteEndpoint,
    "join"           -> joinEndpoint,
    "createApiToken" -> createApiToken
  )
}

object WalletsEndpoints extends WalletsEndpoints
