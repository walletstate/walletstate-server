package online.walletstate.http.api

import online.walletstate.models.Transaction.Page
import online.walletstate.models.api.{CreateAccount, Grouped, UpdateAccount}
import online.walletstate.models.{Account, AppError, Asset, AssetBalance, Transaction}
import zio.Chunk
import zio.http.codec.Doc
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}
import zio.schema.Schema

trait AccountsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "accounts")
      .in[CreateAccount]
      .out[Account](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)
      .??(Doc.h1("Create new account"))

  val list =
    Endpoint(Method.GET / "api" / "accounts")
      .out[List[Account]]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .??(Doc.h1("Get accounts list"))

  val listGrouped =
    Endpoint(Method.GET / "api" / "accounts" / "grouped")
      .out[List[Grouped[Account]]]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .??(Doc.h1("Get grouped accounts list"))

  val get =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path)
      .out[Account]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.AccountNotExist](Status.NotFound)
      .??(Doc.h1("Get an account"))

  val update =
    Endpoint(Method.PUT / "api" / "accounts" / Account.Id.path)
      .in[UpdateAccount]
      .out[Unit](Status.NoContent)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.AccountNotExist](Status.NotFound)
      .??(Doc.h1("Update an account"))

  val listTransactions =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "transactions")
      .query[Option[Transaction.Page.Token]](Transaction.Page.Token.queryCodec.optional)
      .out[Transaction.Page]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.AccountNotExist](Status.NotFound)
      .outError[AppError.BadRequest](Status.BadRequest)
      .??(Doc.h1("Load transactions list for account"))

  val getBalance =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "balance")
      .out[List[AssetBalance]]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.AccountNotExist](Status.NotFound)
      .??(Doc.h1("Get account balance"))

  val endpointsMap = Map(
    "create"           -> create,
    "get"              -> get,
    "update"           -> update,
    "list"             -> list,
    "listGrouped"      -> listGrouped,
    "listTransactions" -> listTransactions,
    "getBalance"       -> getBalance
  )

  val endpoints = Chunk(
    create,
    get,
    update,
    list,
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    listTransactions,
    getBalance
  )

}

object AccountsEndpoints extends AccountsEndpoints
