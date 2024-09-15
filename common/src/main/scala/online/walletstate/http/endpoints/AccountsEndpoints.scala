package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Account, AssetAmount, Grouped, HttpError, Page, Record}
import zio.Chunk
import zio.http.codec.{Doc, HttpCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}
import zio.schema.Schema

trait AccountsEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "Accounts"

  protected val createEndpoint =
    Endpoint(Method.POST / "api" / "accounts")
      .??(Doc.h1("Create new account"))
      .walletStateEndpoint
      .in[Account.Data]
      .out[Account](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  protected val listEndpoint =
    Endpoint(Method.GET / "api" / "accounts")
      .??(Doc.h1("Get accounts list"))
      .walletStateEndpoint
      .out[List[Account]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  protected val listGroupedEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / "grouped")
      .??(Doc.h1("Get grouped accounts list"))
      .walletStateEndpoint
      .out[List[Grouped[Account]]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  protected val getEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path)
      .??(Doc.h1("Get an account"))
      .walletStateEndpoint
      .out[Account]
      .outErrors[Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  protected val updateEndpoint =
    Endpoint(Method.PUT / "api" / "accounts" / Account.Id.path)
      .??(Doc.h1("Update an account"))
      .walletStateEndpoint
      .in[Account.Data]
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  protected val listRecordsEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "records")
      .??(Doc.h1("Load transactions list for account"))
      .walletStateEndpoint
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.Full]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  protected val getBalanceEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "balance")
      .??(Doc.h1("Get account balance"))
      .walletStateEndpoint
      .out[List[AssetAmount]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  override val endpointsMap = Map(
    "create"      -> createEndpoint,
    "get"         -> getEndpoint,
    "update"      -> updateEndpoint,
    "list"        -> listEndpoint,
    "listGrouped" -> listGroupedEndpoint,
    "listRecords" -> listRecordsEndpoint,
    "getBalance"  -> getBalanceEndpoint
  )
}

object AccountsEndpoints extends AccountsEndpoints
