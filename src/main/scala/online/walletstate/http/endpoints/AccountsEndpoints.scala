package online.walletstate.http.endpoints

import online.walletstate.models.api.{CreateAccount, FullRecord, Grouped, UpdateAccount}
import online.walletstate.models.{Account, AppError, AssetAmount, Page}
import zio.Chunk
import zio.http.codec.Doc
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}
import zio.schema.Schema

trait AccountsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "accounts")
      .in[CreateAccount]
      .out[Account](Status.Created)
      .??(Doc.h1("Create new account"))

  val list =
    Endpoint(Method.GET / "api" / "accounts")
      .out[List[Account]]
      .??(Doc.h1("Get accounts list"))

  val listGrouped =
    Endpoint(Method.GET / "api" / "accounts" / "grouped")
      .out[List[Grouped[Account]]]
      .??(Doc.h1("Get grouped accounts list"))

  val get =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path)
      .out[Account]
      .outError[AppError.AccountNotExist](Status.NotFound)
      .??(Doc.h1("Get an account"))

  val update =
    Endpoint(Method.PUT / "api" / "accounts" / Account.Id.path)
      .in[UpdateAccount]
      .out[Unit](Status.NoContent)
      .??(Doc.h1("Update an account"))

  val listRecords =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "records")
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[FullRecord]]
      .??(Doc.h1("Load transactions list for account"))

  val getBalance =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "balance")
      .out[List[AssetAmount]]
      .??(Doc.h1("Get account balance"))

  override val endpointsMap = Map(
    "create"      -> create,
    "get"         -> get,
    "update"      -> update,
    "list"        -> list,
    "listGrouped" -> listGrouped,
    "listRecords" -> listRecords,
    "getBalance"  -> getBalance
  )

  override val endpoints = Chunk(
    create,
    get,
    update,
    list,
//    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
//    listRecords, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    getBalance
  )

}

object AccountsEndpoints extends AccountsEndpoints
