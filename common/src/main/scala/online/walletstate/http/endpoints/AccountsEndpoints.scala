package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Account, AssetAmount, Grouped, HttpError, Page, Record}
import zio.Chunk
import zio.http.codec.{Doc, HttpCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Method, Status}
import zio.schema.Schema

trait AccountsEndpoints extends WalletStateEndpoints {

  protected val createEndpoint =
    Endpoint(Method.POST / "api" / "accounts")
      .??(Doc.h1("Create new account"))
      .withAuth
      .in[Account.Data]
      .out[Account](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  protected val listEndpoint =
    Endpoint(Method.GET / "api" / "accounts")
      .??(Doc.h1("Get accounts list"))
      .withAuth
      .out[List[Account]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  protected val listGroupedEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / "grouped")
      .??(Doc.h1("Get grouped accounts list"))
      .withAuth
      .out[List[Grouped[Account]]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  protected val getEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path)
      .??(Doc.h1("Get an account"))
      .withAuth
      .out[Account]
      .outErrors[Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  protected val updateEndpoint =
    Endpoint(Method.PUT / "api" / "accounts" / Account.Id.path)
      .??(Doc.h1("Update an account"))
      .withAuth
      .in[Account.Data]
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  protected val listRecordsEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "records")
      .??(Doc.h1("Load transactions list for account"))
      .withAuth
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.Full]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  protected val getBalanceEndpoint =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "balance")
      .??(Doc.h1("Get account balance"))
      .withAuth
      .out[List[AssetAmount]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create"      -> createEndpoint,
    "get"         -> getEndpoint,
    "update"      -> updateEndpoint,
    "list"        -> listEndpoint,
    "listGrouped" -> listGroupedEndpoint,
    "listRecords" -> listRecordsEndpoint,
    "getBalance"  -> getBalanceEndpoint
  )

  override val endpoints = Chunk(
    createEndpoint,
    getEndpoint,
    updateEndpoint,
    listEndpoint,
//    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
//    listRecords, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    getBalanceEndpoint
  )

}

object AccountsEndpoints extends AccountsEndpoints
