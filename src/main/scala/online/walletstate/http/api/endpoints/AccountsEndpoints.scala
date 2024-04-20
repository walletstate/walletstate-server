package online.walletstate.http.api.endpoints

import online.walletstate.models.Transaction.Page
import online.walletstate.models.api.{CreateAccount, Grouped}
import online.walletstate.models.errors.{AccountNotExist, BadRequestError, UnauthorizedError}
import online.walletstate.models.{Account, Asset, Transaction, errors}
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
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)
      .??(Doc.h1("Create new account"))

  val list =
    Endpoint(Method.GET / "api" / "accounts")
      .out[Chunk[Account]]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .??(Doc.h1("Get accounts list"))

  val listGrouped =
    Endpoint(Method.GET / "api" / "accounts" / "grouped")
      .out[Chunk[Grouped[Account]]]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .??(Doc.h1("Get grouped accounts list"))

  val get =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path)
      .out[Account]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[AccountNotExist](Status.NotFound)
      .??(Doc.h1("Get an account"))

  val listTransactions =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "transactions")
      .query[Option[Transaction.Page.Token]](Transaction.Page.Token.queryCodec.optional)
      .out[Transaction.Page]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[AccountNotExist](Status.NotFound)
      .outError[BadRequestError](Status.BadRequest)
      .??(Doc.h1("Load transactions list for account"))

  val getBalance =
    Endpoint(Method.GET / "api" / "accounts" / Account.Id.path / "balance")
      .out[Map[Asset.Id, BigDecimal]]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[AccountNotExist](Status.NotFound)
      .??(Doc.h1("Get account balance"))

  val endpoints = Chunk(
    create,
    get,
    list,
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    listTransactions,
    getBalance
  )

  val endpointsMap = Map(
    "create"           -> create,
    "get"              -> get,
    "list"             -> list,
    "listGrouped"      -> listGrouped,
    "listTransactions" -> listTransactions,
    "getBalance"       -> getBalance
  )
}

object AccountsEndpoints extends AccountsEndpoints
